/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request DTO for resetting password. */
@Schema(
    description = "Reset password request DTO",
    example =
        "{\"token\": \"550e8400-e29b-41d4-a716-446655440000\", "
            + "\"newPassword\": \"NewPass@123\"}")
public record ResetPasswordRequest(
    @Schema(description = "Password reset token", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "El token es requerido")
        String token,
    @Schema(description = "New password", example = "NewPass123@")
        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message =
                "La contraseña debe contener al menos: 8 caracteres, una mayúscula, "
                    + "una minúscula, un número y un carácter especial (@$!%*?&)")
        String newPassword) {}
