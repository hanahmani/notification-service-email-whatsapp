package com.robocare.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class HealthController {

    /**
     * GET /api/notifications/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> h = new LinkedHashMap<>();
        h.put("status", "UP");
        h.put("service", "robocare-notification-service");
        h.put("port", 8081);
        h.put("channels", List.of("EMAIL", "WHATSAPP", "SMS"));
        h.put("endpoints", List.of(
                "POST /api/notifications/email",
                "POST /api/notifications/whatsapp",
                "POST /api/notifications/sms",
                "GET  /api/notifications/health"
        ));
        h.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(h);
    }
}
