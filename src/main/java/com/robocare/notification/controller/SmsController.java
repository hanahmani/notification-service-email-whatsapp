package com.robocare.notification.controller;

import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.dto.SmsRequest;
import com.robocare.notification.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    /**
     * POST /api/notifications/sms
     *
     * { "to": ["+21612345678"], "message": "OTP code: 1234" }
     * { "to": ["+21611111111", "+21622222222"], "message": "Alerte" }
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody SmsRequest request) {

        log.info("POST /api/notifications/sms | to={}", request.getTo());

        NotificationResponse response = smsService.send(request);

        HttpStatus status = "SENT".equals(response.getStatus())
                ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }
}
