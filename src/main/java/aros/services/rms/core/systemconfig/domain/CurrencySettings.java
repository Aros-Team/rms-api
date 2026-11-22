/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import java.util.Currency;
import java.util.Locale;

/**
 * Typed view of the system currency setting.
 *
 * @param code ISO 4217 alphabetic code, for example {@code COP}
 * @param displayName localized display name, for example {@code Colombian Peso}
 * @param symbol currency symbol, for example {@code $}
 * @param defaultFractionDigits number of decimals used when formatting amounts
 */
public record CurrencySettings(
    String code, String displayName, String symbol, int defaultFractionDigits) {

  /**
   * Builds the settings view for an ISO 4217 currency.
   *
   * @param currency the currency
   * @return the typed settings
   */
  public static CurrencySettings from(Currency currency) {
    return new CurrencySettings(
        currency.getCurrencyCode(),
        currency.getDisplayName(Locale.ENGLISH),
        currency.getSymbol(Locale.ENGLISH),
        currency.getDefaultFractionDigits());
  }
}
