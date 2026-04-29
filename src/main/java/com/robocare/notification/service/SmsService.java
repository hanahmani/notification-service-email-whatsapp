package com.robocare.notification.service;

import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.dto.SmsRequest;
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
public class SmsService {

    @Value("${sms.api-id:disabled}")
    private String apiId;

    @Value("${sms.api-url:https://api.tunisiesms.tn/sms/send}")
    private String apiUrl;

    @Value("${sms.sender-name:RoboCare}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envoie un SMS a un ou plusieurs destinataires via TunisieSMS.
     */
    public NotificationResponse send(SmsRequest request) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        int success = 0;
        int fail = 0;
        String lastError = null;

        if ("disabled".equals(apiId)) {
            return NotificationResponse.builder()
                    .id(id).channel("SMS").to(request.getTo())
                    .status("FAILED").successCount(0)
                    .failCount(request.getTo().size())
                    .timestamp(LocalDateTime.now())
                    .errorDetails("SMS non configure. "
                        + "Definir TUNISIESMS_ID dans .env")
                    .build();
        }

        log.info("[{}][SMS] Envoi vers {} destinataire(s)",
                id, request.getTo().size());

        for (String recipient : request.getTo()) {
            try {
                String cleanNumber = recipient.replace("+", "");
                sendSms(cleanNumber, request.getMessage());
                success++;
                log.info("[{}][SMS] SENT vers {}", id, recipient);
            } catch (Exception e) {
                fail++;
                lastError = e.getMessage();
                log.error("[{}][SMS] FAILED vers {} : {}",
                        id, recipient, e.getMessage());
            }
        }

        String status = fail == 0 ? "SENT"
                : success == 0 ? "FAILED" : "PARTIAL";

        return NotificationResponse.builder()
                .id(id)
                .channel("SMS")
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
     * Envoie un SMS via l'API TunisieSMS.
     */
    private void sendSms(String to, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("id", apiId);
        body.put("to", to);
        body.put("from", senderName);
        body.put("msg", message);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("TunisieSMS error: "
                    + resp.getStatusCode() + " " + resp.getBody());
        }

        log.info("[SMS] TunisieSMS reponse : {}", resp.getBody());
    }
}
