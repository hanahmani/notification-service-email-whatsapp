package com.robocare.notification.controller;

import com.robocare.notification.dto.NotificationRequest;
import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * POST /notifications — Envoyer un email
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody NotificationRequest request) {

        log.info("POST /notifications | type={} dest={}",
                request.getType(), request.getRecipient());

        NotificationResponse response = notificationService.send(request);

        if ("SENT".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /notifications — Historique
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    /**
     * GET /notifications/{id} — Detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable String id) {
        NotificationResponse resp = notificationService.getById(id);
        if (resp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /notifications/stats — Statistiques
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(notificationService.getStats());
    }

    /**
     * GET /notifications/health — Healthcheck
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> h = new LinkedHashMap<>();
        h.put("status", "UP");
        h.put("service", "robocare-notification-service");
        h.put("port", 8081);
        h.put("channels", List.of("EMAIL"));
        h.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(h);
    }
}
