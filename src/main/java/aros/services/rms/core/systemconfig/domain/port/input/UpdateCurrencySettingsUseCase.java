/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.CurrencySettings;

/** Use case for validating and persisting the system currency. */
public interface UpdateCurrencySettingsUseCase {

  /**
   * Validates an ISO 4217 code, persists it and refreshes the currency provider.
   *
   * @param code the ISO 4217 alphabetic code
   * @return the persisted currency settings
   */
  CurrencySettings update(String code);
}
