package com.robocare.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * POST /api/notifications/email
 *
 * Exemple un destinataire :
 * { "to": ["user@gmail.com"], "subject": "Alerte", "message": "Texte" }
 *
 * Exemple plusieurs destinataires :
 * { "to": ["user1@gmail.com", "user2@gmail.com"], "subject": "Hello", "message": "Bulk" }
 *
 * Avec PDF :
 * { "to": ["user@gmail.com"], "subject": "Rapport", "message": "Voir PJ", "fileUrl": "https://..." }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotEmpty(message = "Au moins un destinataire requis")
    private List<String> to;

    private String subject;

    @NotBlank(message = "Le message est obligatoire")
    private String message;

    private String fileUrl;
}
