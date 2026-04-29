package com.robocare.notification.sender;

import com.robocare.notification.dto.NotificationRequest;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender implements NotificationSender {

    private final JavaMailSender mailSender;

    @Override
    public boolean send(NotificationRequest request) throws Exception {

        // Si PDF joint → envoi avec piece jointe
        if (request.getAttachmentUrl() != null
                && !request.getAttachmentUrl().isBlank()) {
            return sendWithAttachment(request);
        }

        // Sinon → envoi simple
        return sendSimple(request);
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }

    /**
     * Envoi email simple (sans piece jointe).
     */
    private boolean sendSimple(NotificationRequest request) throws Exception {
        log.info("[EMAIL] Envoi simple vers {}", request.getRecipient());

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(request.getRecipient());
        mail.setSubject(buildSubject(request));
        mail.setText(buildBody(request));

        mailSender.send(mail);

        log.info("[EMAIL] Envoye avec succes vers {}", request.getRecipient());
        return true;
    }

    /**
     * Envoi email avec piece jointe PDF.
     */
    private boolean sendWithAttachment(NotificationRequest request) throws Exception {
        log.info("[EMAIL+PDF] Envoi vers {} | pdf={}",
                request.getRecipient(), request.getAttachmentUrl());

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage, true, "UTF-8");

        helper.setTo(request.getRecipient());
        helper.setSubject(buildSubject(request));
        helper.setText(buildBody(request));

        // Telecharger et attacher le PDF
        try {
            String pdfUrl = request.getAttachmentUrl();
            String fileName = extractFileName(pdfUrl);

            URL url = new URI(pdfUrl).toURL();
            InputStream inputStream = url.openStream();
            byte[] pdfBytes = inputStream.readAllBytes();
            inputStream.close();

            InputStreamSource source = new ByteArrayResource(pdfBytes);
            helper.addAttachment(fileName, source, "application/pdf");

            log.info("[EMAIL+PDF] PDF attache : {} ({} octets)",
                    fileName, pdfBytes.length);
        } catch (Exception e) {
            log.error("[EMAIL+PDF] Erreur PDF : {} — envoi sans PJ",
                    e.getMessage());
        }

        mailSender.send(mimeMessage);

        log.info("[EMAIL+PDF] Envoye avec succes vers {}",
                request.getRecipient());
        return true;
    }

    private String extractFileName(String url) {
        if (url == null) return "document.pdf";
        String name = url.substring(url.lastIndexOf('/') + 1);
        if (!name.toLowerCase().endsWith(".pdf")) name = name + ".pdf";
        if (name.isBlank() || name.equals(".pdf")) return "document.pdf";
        return name;
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
                    sb.append(key).append(" : ").append(value).append("\n"));
        }
        sb.append("\n-- Equipe RoboCare");
        return sb.toString();
    }
}