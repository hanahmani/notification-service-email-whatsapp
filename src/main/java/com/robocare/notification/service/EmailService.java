package com.robocare.notification.service;

import com.robocare.notification.dto.EmailRequest;
import com.robocare.notification.dto.NotificationResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envoie un email a un ou plusieurs destinataires.
     * Si fileUrl est present, attache le PDF.
     */
    public NotificationResponse send(EmailRequest request) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        int success = 0;
        int fail = 0;
        String lastError = null;

        log.info("[{}][EMAIL] Envoi vers {} destinataire(s)",
                id, request.getTo().size());

        for (String recipient : request.getTo()) {
            try {
                if (request.getFileUrl() != null
                        && !request.getFileUrl().isBlank()) {
                    sendWithPdf(recipient, request);
                } else {
                    sendSimple(recipient, request);
                }
                success++;
                log.info("[{}][EMAIL] SENT vers {}", id, recipient);
            } catch (Exception e) {
                fail++;
                lastError = e.getMessage();
                log.error("[{}][EMAIL] FAILED vers {} : {}",
                        id, recipient, e.getMessage());
            }
        }

        String status = fail == 0 ? "SENT"
                : success == 0 ? "FAILED" : "PARTIAL";

        return NotificationResponse.builder()
                .id(id)
                .channel("EMAIL")
                .to(request.getTo())
                .status(status)
                .successCount(success)
                .failCount(fail)
                .message(request.getMessage())
                .timestamp(LocalDateTime.now())
                .errorDetails(lastError)
                .build();
    }

    private void sendSimple(String recipient, EmailRequest request)
            throws Exception {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(recipient);
        mail.setSubject(request.getSubject() != null
                ? request.getSubject() : "RoboCare Notification");
        mail.setText(request.getMessage());

        mailSender.send(mail);
    }

    private void sendWithPdf(String recipient, EmailRequest request)
            throws Exception {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage, true, "UTF-8");

        helper.setTo(recipient);
        helper.setSubject(request.getSubject() != null
                ? request.getSubject() : "RoboCare Notification");
        helper.setText(request.getMessage());

        // Telecharger et attacher le PDF
        try {
            String pdfUrl = request.getFileUrl();
            String fileName = extractFileName(pdfUrl);

            URL url = new URI(pdfUrl).toURL();
            InputStream is = url.openStream();
            byte[] pdfBytes = is.readAllBytes();
            is.close();

            helper.addAttachment(fileName,
                    new ByteArrayResource(pdfBytes),
                    "application/pdf");

            log.info("[EMAIL] PDF attache : {} ({} octets)",
                    fileName, pdfBytes.length);
        } catch (Exception e) {
            log.warn("[EMAIL] PDF non attache : {} — envoi sans PJ",
                    e.getMessage());
        }

        mailSender.send(mimeMessage);
    }

    private String extractFileName(String url) {
        if (url == null) return "document.pdf";
        String name = url.substring(url.lastIndexOf('/') + 1);
        if (!name.toLowerCase().endsWith(".pdf")) name = name + ".pdf";
        if (name.isBlank() || name.equals(".pdf")) return "document.pdf";
        return name;
    }
}
