/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import java.util.List;

/** Use case for reading the weekly opening hours. */
public interface GetOpeningHoursUseCase {

  /**
   * Returns the seven weekday windows, Monday first.
   *
   * @return the opening hours
   */
  List<OpeningHoursDay> get();
}
