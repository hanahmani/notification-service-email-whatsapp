package com.robocare.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Payload recu des microservices sources.
 *
 * Exemple Email :
 * {
 *   "type": "ALERT_TEMP",
 *   "channel": "EMAIL",
 *   "recipient": "ahmed@gmail.com",
 *   "subject": "Alerte temperature",
 *   "message": "Temperature 45C",
 *   "attachmentUrl": "https://example.com/rapport.pdf"
 * }
 *
 * Exemple WhatsApp :
 * {
 *   "type": "ALERT_TEMP",
 *   "channel": "WHATSAPP",
 *   "recipient": "21612345678",
 *   "message": "Alerte temperature 45C",
 *   "attachmentUrl": "https://example.com/rapport.pdf"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    @NotBlank(message = "Le canal est obligatoire (EMAIL ou WHATSAPP)")
    private String channel;

    @NotBlank(message = "Le destinataire est obligatoire")
    private String recipient;

    private String subject;

    private String message;

    private Map<String, String> data;

    private String attachmentUrl;
}