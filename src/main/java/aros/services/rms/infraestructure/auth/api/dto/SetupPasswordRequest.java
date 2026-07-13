/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request DTO for setting up a new account password. */
@Schema(description = "Request payload to set up a new account password using a setup token")
public record SetupPasswordRequest(
    @Schema(
            description = "Setup token received by email",
            example = "abc123def456",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Token is required")
        String token,
    @Schema(description = "Full name (required for new accounts)", example = "Jane Doe")
        String name,
    @Schema(description = "Document number (required for new accounts)", example = "1234567890")
        String document,
    @Schema(
            description =
                "New password (8-64 chars; must contain uppercase, lowercase,"
                    + " digit and special char)",
            example = "NewP@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\p{Punct})[\\p{Graph}]{8,64}$",
            message =
                "Password must contain at least: one uppercase, one lowercase,"
                    + " one number, and one special character")
        String newPassword) {}
