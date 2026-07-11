/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request DTO for setting up a new account password. */
public record SetupPasswordRequest(
    @NotBlank(message = "Token is required") String token,
    String name,
    String document,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\p{Punct})[\\p{Graph}]{8,64}$",
            message =
                "Password must contain at least: one uppercase, one lowercase,"
                    + " one number, and one special character")
        String newPassword) {}
