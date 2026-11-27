/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request payload to change the system currency. */
@Schema(description = "ISO 4217 currency code to activate")
public record CurrencySettingsRequest(
    @Schema(description = "ISO 4217 alphabetic code", example = "COP")
        @NotBlank(message = "El código de moneda es obligatorio")
        @Pattern(
            regexp = "[A-Za-z]{3}",
            message = "El código de moneda debe constar de tres letras (ISO 4217)")
        String code) {}
