package com.robocare.notification.controller;

import com.robocare.notification.dto.EmailRequest;
import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * POST /api/notifications/email
     *
     * { "to": ["user@gmail.com"], "subject": "...", "message": "..." }
     * { "to": ["a@b.com","c@d.com"], "subject": "...", "message": "...", "fileUrl": "https://..." }
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody EmailRequest request) {

        log.info("POST /api/notifications/email | to={} | pdf={}",
                request.getTo(),
                request.getFileUrl() != null ? "oui" : "non");

        NotificationResponse response = emailService.send(request);

        HttpStatus status = "SENT".equals(response.getStatus())
                ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }
}
