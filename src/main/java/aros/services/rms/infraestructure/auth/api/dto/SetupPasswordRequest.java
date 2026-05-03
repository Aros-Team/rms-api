/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO for setting up a new account password. */
public record SetupPasswordRequest(
    @NotBlank(message = "Token is required") String token,
    String name,
    String document,
    @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String newPassword) {}
