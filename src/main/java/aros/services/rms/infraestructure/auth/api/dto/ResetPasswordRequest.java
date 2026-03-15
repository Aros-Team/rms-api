/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    description = "Reset password request DTO",
    example =
        """
        {
          "token": "550e8400-e29b-41d4-a716-446655440000",
          "newPassword": "newpassword123"
        }
        """)
public record ResetPasswordRequest(
    @Schema(description = "Password reset token", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "El token es requerido") String token,
    @Schema(description = "New password", example = "newpassword123")
        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 1, message = "La contraseña no puede estar vacía") String newPassword) {}
