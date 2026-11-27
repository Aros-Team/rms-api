/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request DTO for changing password. */
@Schema(description = "Request payload to change the authenticated user's password")
public record ChangePasswordRequest(
    @Schema(description = "Current password (8-64 chars)", example = "OldP@ssw0rd")
        @NotBlank(message = "La contraseña actual es obligatoria")
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
        String currentPassword,
    @Schema(
            description =
                "New password (8-64 chars; must contain uppercase, lowercase,"
                    + " digit and special char)",
            example = "NewP@ssw0rd!")
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\p{Punct})[\\p{Graph}]{8,64}$",
            message =
                "La nueva contraseña debe contener al menos: una mayúscula, una minúscula, "
                    + "un número y un carácter especial")
        String newPassword) {}
