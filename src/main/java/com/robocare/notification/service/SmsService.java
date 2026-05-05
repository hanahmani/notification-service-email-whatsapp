package com.robocare.notification.service;

import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.dto.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class SmsService {

    @Value("${sms.api-id:disabled}")
    private String apiId;

    @Value("${sms.api-url}")
    private String apiUrl;

    @Value("${sms.sender-name}")
    private String senderName;


    private final RestTemplate restTemplate = new RestTemplate();

    public NotificationResponse send(SmsRequest request) {
        String id = UUID.randomUUID().toString().substring(0, 8);

        int success = 0;
        int fail = 0;
        String lastError = null;

        if ("disabled".equals(apiId)) {
            return NotificationResponse.builder()
                    .id(id)
                    .channel("SMS")
                    .to(request.getTo())
                    .status("FAILED")
                    .successCount(0)
                    .failCount(request.getTo().size())
                    .timestamp(LocalDateTime.now())
                    .errorDetails("SMS non configuré")
                    .build();
        }

        for (String recipient : request.getTo()) {
            try {
                String cleanNumber = recipient.replace("+", "");
                sendSms(cleanNumber, request.getMessage());
                success++;
            } catch (Exception e) {
                fail++;
                lastError = e.getMessage();
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

    private void sendSms(String to, String message) {
        log.info("Sending SMS to {}", senderName);
        String date = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String time = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        String url = apiUrl
                + "?fct=sms"
                + "&key=" + apiId
                + "&mobile=" + to
                + "&sms=" + message.replace(" ", "+")
                + "&sender=" + senderName
                + "&date=" + date
                + "&heure=" + time;

        log.info("[SMS] URL: {}", url);

        ResponseEntity<String> response =
                new RestTemplate().getForEntity(url, String.class); // ⚠️ tu n’as pas défini restTemplate

        log.info("[SMS] RESPONSE: {}", response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erreur API SMS");
        }
    }
}