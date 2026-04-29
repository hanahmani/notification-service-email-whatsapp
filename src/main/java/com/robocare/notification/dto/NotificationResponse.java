package com.robocare.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String type;
    private String channel;
    private String recipient;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private String errorDetails;
}
