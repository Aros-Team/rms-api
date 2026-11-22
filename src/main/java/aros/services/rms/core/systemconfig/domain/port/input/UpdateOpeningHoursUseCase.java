/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import java.util.List;

/** Use case for replacing the weekly opening hours. */
public interface UpdateOpeningHoursUseCase {

  /**
   * Replaces the seven weekday windows.
   *
   * @param days the new windows, one per weekday
   * @return the persisted windows, Monday first
   */
  List<OpeningHoursDay> update(List<OpeningHoursDay> days);
}
