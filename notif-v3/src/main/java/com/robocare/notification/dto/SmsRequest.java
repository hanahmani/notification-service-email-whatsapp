package com.robocare.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * POST /api/notifications/sms
 *
 * Un destinataire :
 * { "to": ["+21612345678"], "message": "OTP code: 1234" }
 *
 * Plusieurs :
 * { "to": ["+21611111111", "+21622222222"], "message": "Alerte RoboCare" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    @NotEmpty(message = "Au moins un destinataire requis")
    private List<String> to;

    @NotBlank(message = "Le message est obligatoire")
    private String message;
}
