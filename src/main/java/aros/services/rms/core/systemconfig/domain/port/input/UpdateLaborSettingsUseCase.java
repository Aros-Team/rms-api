/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.LaborSettings;

/** Use case for validating and persisting labor costing settings. */
public interface UpdateLaborSettingsUseCase {

  /**
   * Validates and persists the labor settings.
   *
   * @param settings the new settings
   * @return the persisted settings
   */
  LaborSettings update(LaborSettings settings);
}
