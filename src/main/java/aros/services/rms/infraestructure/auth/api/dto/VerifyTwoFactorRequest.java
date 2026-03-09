/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Two-factor authentication verification request")
public record VerifyTwoFactorRequest(
    @Schema(description = "6-digit verification code", example = "123456")
        @NotBlank(message = "El código es requerido") @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos") String code,
    @Schema(description = "Device identifier", example = "device-123")
        @NotBlank(message = "Se requiere el identifador del dispositivo") String deviceHash) {}
