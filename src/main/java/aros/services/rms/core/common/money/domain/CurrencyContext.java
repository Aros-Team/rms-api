/* (C) 2026 */

package aros.services.rms.core.common.money.domain;

import java.util.Currency;

/**
 * Process-wide currency used by domain defaults.
 *
 * <p>The authoritative currency is the {@code default_currency} system setting, exposed at runtime
 * through the {@code CurrencyProvider} port. Infrastructure installs that value here at startup and
 * refreshes it whenever the currency setting changes, so domain objects that need a zero amount no
 * longer hardcode an ISO code. The bootstrap code is the only literal kept in the codebase and is
 * used before the application context has loaded any persisted setting (for example in unit tests),
 * so runtime behaviour always follows the configured currency.
 */
public final class CurrencyContext {

  /** Bootstrap ISO 4217 code used until a persisted setting is installed. */
  public static final String BOOTSTRAP_CURRENCY_CODE = "COP";

  private static volatile Currency current = Currency.getInstance(BOOTSTRAP_CURRENCY_CODE);

  private CurrencyContext() {}

  /**
   * Returns the currency currently installed for domain defaults.
   *
   * @return the installed currency
   */
  public static Currency get() {
    return current;
  }

  /**
   * Installs the currency used by domain defaults.
   *
   * @param currency the currency to install; {@code null} is ignored
   */
  public static void set(Currency currency) {
    if (currency != null) {
      current = currency;
    }
  }
}
