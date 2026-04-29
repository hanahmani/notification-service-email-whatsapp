package com.robocare.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * POST /api/notifications/whatsapp
 *
 * Message simple :
 * { "to": ["+21612345678"], "message": "Alerte temperature" }
 *
 * Plusieurs destinataires :
 * { "to": ["+21611111111", "+21622222222"], "message": "Maintenance ce soir" }
 *
 * Avec PDF :
 * { "to": ["+21612345678"], "message": "Voir rapport", "type": "PDF", "fileUrl": "https://..." }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappRequest {

    @NotEmpty(message = "Au moins un destinataire requis")
    private List<String> to;

    @NotBlank(message = "Le message est obligatoire")
    private String message;

    private String type;      // "PDF" si document joint, null sinon

    private String fileUrl;   // URL du PDF si type = "PDF"
}
