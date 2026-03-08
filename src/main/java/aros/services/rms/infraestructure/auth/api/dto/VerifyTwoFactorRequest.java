/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyTwoFactorRequest(
    @NotBlank(message = "El código es requerido") @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos") String code,
    @NotBlank(message = "Se requiere el identifador del dispositivo") String deviceHash) {}
