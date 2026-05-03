/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for user login. */
@Schema(
    description = "Login request DTO",
    example =
        "{\"username\": \"quintosteven590@gmail.com\", "
            + "\"password\": \"123\", \"deviceHash\": \"device-123\"}")
public record LoginRequest(
    @Schema(description = "User email", example = "quintosteven590@gmail.com")
        @NotBlank(message = "El username es requerido")
        @Email(message = "Formato de email inválido")
        String username,
    @Schema(description = "User password", example = "123")
        @NotBlank(message = "La contraseña es requerida")
        String password,
    @Schema(description = "Device hash for 2FA", example = "device-123") String deviceHash) {}
