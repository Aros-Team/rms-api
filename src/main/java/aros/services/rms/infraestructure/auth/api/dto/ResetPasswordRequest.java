/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
    description = "Reset password request DTO",
    example =
        """
        {
          "token": "550e8400-e29b-41d4-a716-446655440000",
          "newPassword": "NewPass@123"
        }
        """)
public record ResetPasswordRequest(
    @Schema(description = "Token de recuperación de contraseña", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "El token de recuperación es requerido")
        String token,
    @Schema(description = "Nueva contraseña", example = "NewPass@123")
        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 8, max = 20, message = "La nueva contraseña debe tener entre 8 y 20 caracteres")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message =
                "La nueva contraseña debe contener al menos: 1 mayúscula, 1 minúscula, 1 número y 1 símbolo (@$!%*?&)")
        String newPassword) {}