/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.output;

import java.util.Currency;

/** Output port for retrieving the system-wide currency. */
public interface CurrencyProvider {

  /**
   * Returns the current system currency.
   *
   * @return the currency
   */
  Currency getCurrency();
}
