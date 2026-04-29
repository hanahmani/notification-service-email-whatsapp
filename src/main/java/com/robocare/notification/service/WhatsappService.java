package com.robocare.notification.service;

import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.dto.WhatsappRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class WhatsappService {

    @Value("${whatsapp.token:disabled}")
    private String token;

    @Value("${whatsapp.phone-id:disabled}")
    private String phoneId;

    @Value("${whatsapp.api-url:https://graph.facebook.com/v21.0/1101097773077897/messages}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envoie un message WhatsApp a un ou plusieurs destinataires.
     * Si type = "PDF" et fileUrl present, envoie le document + message.
     */
    public NotificationResponse send(WhatsappRequest request) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        int success = 0;
        int fail = 0;
        String lastError = null;

        if ("disabled".equals(token) || "disabled".equals(phoneId)) {
            return NotificationResponse.builder()
                    .id(id).channel("WHATSAPP").to(request.getTo())
                    .status("FAILED").successCount(0)
                    .failCount(request.getTo().size())
                    .timestamp(LocalDateTime.now())
                    .errorDetails("WhatsApp non configure")
                    .build();
        }

        log.info("[{}][WHATSAPP] Envoi vers {} destinataire(s)",
                id, request.getTo().size());

        boolean hasPdf = "PDF".equalsIgnoreCase(request.getType())
                && request.getFileUrl() != null
                && !request.getFileUrl().isBlank();

        for (String recipient : request.getTo()) {
            try {
                // Nettoyer le numero (enlever le + si present)
                String cleanNumber = recipient.replace("+", "");

                if (hasPdf) {
                    sendDocument(cleanNumber, request);
                }
                sendText(cleanNumber, request.getMessage());

                success++;
                log.info("[{}][WHATSAPP] SENT vers {}", id, recipient);
            } catch (Exception e) {
                fail++;
                lastError = e.getMessage();
                log.error("[{}][WHATSAPP] FAILED vers {} : {}",
                        id, recipient, e.getMessage());
            }
        }

        String status = fail == 0 ? "SENT"
                : success == 0 ? "FAILED" : "PARTIAL";

        return NotificationResponse.builder()
                .id(id)
                .channel("WHATSAPP")
                .to(request.getTo())
                .status(status)
                .successCount(success)
                .failCount(fail)
                .message(request.getMessage())
                .timestamp(LocalDateTime.now())
                .errorDetails(lastError)
                .build();
    }

    /**
     * Envoie un message texte simple.
     */
    private void sendText(String to, String message) {
        String url = apiUrl + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", to);
        body.put("type", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("body", message);
        text.put("preview_url", false);
        body.put("text", text);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("WhatsApp API error: "
                    + resp.getStatusCode());
        }
    }

    /**
     * Envoie un document PDF via WhatsApp.
     * Le PDF doit etre accessible via URL publique.
     */
    private void sendDocument(String to, WhatsappRequest request) {
        String url = apiUrl + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", to);
        body.put("type", "document");

        Map<String, Object> document = new HashMap<>();
        document.put("link", request.getFileUrl());
        document.put("filename", extractFileName(request.getFileUrl()));
        document.put("caption", request.getMessage());
        body.put("document", document);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("WhatsApp PDF error: "
                    + resp.getStatusCode());
        }

        log.info("[WHATSAPP] PDF envoye : {}", request.getFileUrl());
    }

    private String extractFileName(String url) {
        if (url == null) return "document.pdf";
        String name = url.substring(url.lastIndexOf('/') + 1);
        if (!name.toLowerCase().endsWith(".pdf")) name = name + ".pdf";
        if (name.isBlank() || name.equals(".pdf")) return "document.pdf";
        return name;
    }
}
