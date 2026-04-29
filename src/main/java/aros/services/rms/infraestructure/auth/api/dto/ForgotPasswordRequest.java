/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Forgot password request DTO", example = "{\"email\": \"user@example.com\"}")
public record ForgotPasswordRequest(
    @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "El email es requerido") @Email(message = "Formato de email inválido") String email) {}
