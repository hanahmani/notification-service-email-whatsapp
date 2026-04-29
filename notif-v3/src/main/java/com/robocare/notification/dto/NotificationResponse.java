package com.robocare.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String channel;         // EMAIL, WHATSAPP, SMS
    private List<String> to;
    private String status;          // SENT, FAILED, PARTIAL
    private int successCount;
    private int failCount;
    private String message;
    private LocalDateTime timestamp;
    private String errorDetails;
}
