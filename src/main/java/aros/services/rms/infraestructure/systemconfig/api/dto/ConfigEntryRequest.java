/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO to update a configuration entry. */
@Schema(
    description = "Request to update a configuration entry",
    example = "{\"key\": \"default_currency\", \"value\": \"USD\"}")
public record ConfigEntryRequest(
    @Schema(description = "Configuration key", example = "default_currency")
        @NotBlank(message = "Key must not be blank")
        @Size(max = 120, message = "Key must not exceed 120 characters")
        String key,
    @Schema(description = "Configuration value", example = "USD")
        @NotBlank(message = "Value must not be blank")
        @Size(max = 500, message = "Value must not exceed 500 characters")
        String value) {}
