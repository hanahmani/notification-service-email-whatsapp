package com.robocare.notification.sender;

import com.robocare.notification.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WhatsAppSender implements NotificationSender {

    @Value("${whatsapp.token:disabled}")
    private String token;

    @Value("${whatsapp.phone-id:disabled}")
    private String phoneId;

    @Value("${whatsapp.api-url:https://graph.facebook.com/v25.0/1101097773077897/messages}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean send(NotificationRequest request) throws Exception {
        if ("disabled".equals(token) || "disabled".equals(phoneId)) {
            throw new RuntimeException("WhatsApp non configure");
        }

        log.info("[WHATSAPP] Envoi vers {} | type={}",
                request.getRecipient(), request.getType());

        String url = apiUrl + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", request.getRecipient());
        body.put("type", "text");

        String msg = request.getMessage();
        if (msg == null || msg.isBlank()) {
            StringBuilder sb = new StringBuilder();
            sb.append("*RoboCare - ").append(request.getType()).append("*\n\n");
            if (request.getData() != null) {
                request.getData().forEach((k, v) ->
                        sb.append(k).append(" : ").append(v).append("\n"));
            }
            msg = sb.toString();
        }

        Map<String, Object> text = new HashMap<>();
        text.put("body", msg);
        text.put("preview_url", false);
        body.put("text", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (resp.getStatusCode().is2xxSuccessful()) {
            log.info("[WHATSAPP] Envoye avec succes vers {}", request.getRecipient());
            return true;
        }
        return false;
    }

    @Override
    public String getChannel() {
        return "WHATSAPP";
    }
}