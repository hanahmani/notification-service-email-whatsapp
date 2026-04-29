package com.robocare.notification.controller;

import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.dto.WhatsappRequest;
import com.robocare.notification.service.WhatsappService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications/whatsapp")
@RequiredArgsConstructor
public class WhatsappController {

    private final WhatsappService whatsappService;

    /**
     * POST /api/notifications/whatsapp
     *
     * Texte :  { "to": ["+21612345678"], "message": "Alerte" }
     * PDF :    { "to": ["+21612345678"], "message": "Voir PJ", "type": "PDF", "fileUrl": "https://..." }
     * Bulk :   { "to": ["+21611111111", "+21622222222"], "message": "Maintenance" }
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody WhatsappRequest request) {

        log.info("POST /api/notifications/whatsapp | to={} | type={}",
                request.getTo(),
                request.getType() != null ? request.getType() : "TEXT");

        NotificationResponse response = whatsappService.send(request);

        HttpStatus status = "SENT".equals(response.getStatus())
                ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }
}
