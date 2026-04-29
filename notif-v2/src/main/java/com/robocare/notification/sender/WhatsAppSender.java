package com.robocare.notification.sender;

import com.robocare.notification.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Envoi WhatsApp via Meta Business API.
 * Supporte :
 *   - Message texte simple
 *   - Message texte + document PDF en piece jointe
 */
@Slf4j
@Component
public class WhatsAppSender implements NotificationSender {

    @Value("${whatsapp.token:disabled}")
    private String token;

    @Value("${whatsapp.phone-id:disabled}")
    private String phoneId;

    @Value("${whatsapp.api-url:https://graph.facebook.com/v21.0/1101097773077897/messages}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean send(NotificationRequest request) throws Exception {

        if ("disabled".equals(token) || "disabled".equals(phoneId)) {
            throw new RuntimeException("WhatsApp non configure. "
                    + "Definir WHATSAPP_TOKEN et WHATSAPP_PHONE_ID.");
        }

        // Si PDF joint → envoyer le document d'abord, puis le message texte
        if (request.getAttachmentUrl() != null
                && !request.getAttachmentUrl().isBlank()) {
            return sendWithDocument(request);
        }

        // Sinon → message texte simple
        return sendText(request);
    }

    @Override
    public String getChannel() {
        return "WHATSAPP";
    }

    /**
     * Envoi message texte simple.
     */
    private boolean sendText(NotificationRequest request) throws Exception {
        log.info("[WHATSAPP] Envoi texte vers {} | type={}",
                request.getRecipient(), request.getType());

        String url = apiUrl + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", request.getRecipient());
        body.put("type", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("body", buildMessage(request));
        text.put("preview_url", false);
        body.put("text", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("[WHATSAPP] Texte envoye vers {}", request.getRecipient());
            return true;
        }
        return false;
    }

    /**
     * Envoi document PDF via WhatsApp.
     *
     * Meta API supporte l'envoi de documents via une URL publique.
     * Le PDF doit etre accessible publiquement sur Internet.
     *
     * On envoie d'abord le PDF, puis le message texte.
     */
    private boolean sendWithDocument(NotificationRequest request) throws Exception {
        log.info("[WHATSAPP+PDF] Envoi vers {} | pdf={}",
                request.getRecipient(), request.getAttachmentUrl());

        String url = apiUrl + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        // --- 1. Envoyer le document PDF ---
        Map<String, Object> docBody = new HashMap<>();
        docBody.put("messaging_product", "whatsapp");
        docBody.put("to", request.getRecipient());
        docBody.put("type", "document");

        Map<String, Object> document = new HashMap<>();
        document.put("link", request.getAttachmentUrl());
        document.put("filename", extractFileName(request.getAttachmentUrl()));

        // Ajouter une legende au document
        String caption = "RoboCare - " + request.getType();
        if (request.getSubject() != null && !request.getSubject().isBlank()) {
            caption = request.getSubject();
        }
        document.put("caption", caption);

        docBody.put("document", document);

        HttpEntity<Map<String, Object>> docEntity =
                new HttpEntity<>(docBody, headers);

        ResponseEntity<String> docResponse = restTemplate.exchange(
                url, HttpMethod.POST, docEntity, String.class);

        if (!docResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[WHATSAPP+PDF] Echec envoi PDF | status={}",
                    docResponse.getStatusCode());
            return false;
        }

        log.info("[WHATSAPP+PDF] PDF envoye avec succes");

        // --- 2. Envoyer le message texte (si present) ---
        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            Map<String, Object> textBody = new HashMap<>();
            textBody.put("messaging_product", "whatsapp");
            textBody.put("to", request.getRecipient());
            textBody.put("type", "text");

            Map<String, Object> text = new HashMap<>();
            text.put("body", buildMessage(request));
            text.put("preview_url", false);
            textBody.put("text", text);

            HttpEntity<Map<String, Object>> textEntity =
                    new HttpEntity<>(textBody, headers);

            restTemplate.exchange(url, HttpMethod.POST,
                    textEntity, String.class);

            log.info("[WHATSAPP+PDF] Message texte envoye");
        }

        log.info("[WHATSAPP+PDF] Tout envoye vers {}",
                request.getRecipient());
        return true;
    }

    /**
     * Extrait le nom du fichier depuis l'URL.
     */
    private String extractFileName(String url) {
        if (url == null) return "document.pdf";
        String name = url.substring(url.lastIndexOf('/') + 1);
        if (!name.toLowerCase().endsWith(".pdf")) name = name + ".pdf";
        if (name.isBlank() || name.equals(".pdf")) return "document.pdf";
        return name;
    }

    /**
     * Construit le message texte.
     */
    private String buildMessage(NotificationRequest request) {
        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            return request.getMessage();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("*RoboCare - ").append(request.getType()).append("*\n\n");
        if (request.getData() != null) {
            request.getData().forEach((key, value) ->
                    sb.append(key).append(" : ").append(value).append("\n"));
        }
        sb.append("\n-- Equipe RoboCare");
        return sb.toString();
    }
}