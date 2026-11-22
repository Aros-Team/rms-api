/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.CurrencySettings;

/** Use case for reading the typed currency setting. */
public interface GetCurrencySettingsUseCase {

  /**
   * Returns the configured currency.
   *
   * @return the currency settings
   */
  CurrencySettings get();
}
