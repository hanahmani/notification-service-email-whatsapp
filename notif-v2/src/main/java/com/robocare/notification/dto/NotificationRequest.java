package com.robocare.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    @NotBlank(message = "Le destinataire est obligatoire")
    private String recipient;

    private String subject;

    private String message;

    private Map<String, String> data;
}
