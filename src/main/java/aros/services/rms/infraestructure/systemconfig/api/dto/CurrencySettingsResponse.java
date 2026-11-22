/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.CurrencySettings;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response payload describing the active currency. */
@Schema(description = "Active system currency")
public record CurrencySettingsResponse(
    @Schema(description = "ISO 4217 alphabetic code", example = "COP") String code,
    @Schema(description = "Localized display name", example = "Colombian Peso") String displayName,
    @Schema(description = "Currency symbol", example = "$") String symbol,
    @Schema(description = "Number of fraction digits", example = "2") int defaultFractionDigits) {

  /**
   * Builds a response from the domain settings.
   *
   * @param settings the currency settings
   * @return the response payload
   */
  public static CurrencySettingsResponse fromDomain(CurrencySettings settings) {
    return new CurrencySettingsResponse(
        settings.code(),
        settings.displayName(),
        settings.symbol(),
        settings.defaultFractionDigits());
  }
}
