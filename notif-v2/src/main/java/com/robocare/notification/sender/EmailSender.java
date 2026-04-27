package com.robocare.notification.sender;

import com.robocare.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender implements NotificationSender {

    private final JavaMailSender mailSender;

    @Override
    public boolean send(NotificationRequest request) throws Exception {
        log.info("[EMAIL] Envoi vers {} | type={}", request.getRecipient(), request.getType());

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(request.getRecipient());
        mail.setSubject(buildSubject(request));
        mail.setText(buildBody(request));

        mailSender.send(mail);

        log.info("[EMAIL] Envoye avec succes vers {}", request.getRecipient());
        return true;
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }

    private String buildSubject(NotificationRequest request) {
        if (request.getSubject() != null && !request.getSubject().isBlank()) {
            return request.getSubject();
        }
        return "RoboCare - " + request.getType();
    }

    private String buildBody(NotificationRequest request) {
        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            return request.getMessage();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Notification RoboCare\n\n");
        sb.append("Type : ").append(request.getType()).append("\n");

        if (request.getData() != null) {
            request.getData().forEach((key, value) ->
                sb.append(key).append(" : ").append(value).append("\n")
            );
        }

        sb.append("\n-- Equipe RoboCare");
        return sb.toString();
    }
}
