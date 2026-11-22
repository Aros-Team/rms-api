/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.LaborSettings;

/** Use case for reading the typed labor costing settings. */
public interface GetLaborSettingsUseCase {

  /**
   * Returns the current labor settings.
   *
   * @return the labor settings
   */
  LaborSettings get();
}
