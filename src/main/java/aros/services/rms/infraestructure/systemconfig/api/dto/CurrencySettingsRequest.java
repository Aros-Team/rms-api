/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request payload to change the system currency. */
@Schema(description = "ISO 4217 currency code to activate")
public record CurrencySettingsRequest(
    @Schema(description = "ISO 4217 alphabetic code", example = "COP")
        @NotBlank(message = "Currency code is required")
        @Pattern(
            regexp = "[A-Za-z]{3}",
            message = "Currency code must be a three-letter ISO 4217 code")
        String code) {}
