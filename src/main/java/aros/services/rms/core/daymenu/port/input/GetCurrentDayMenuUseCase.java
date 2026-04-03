/* (C) 2026 */
package aros.services.rms.core.daymenu.port.input;

import aros.services.rms.core.daymenu.domain.DayMenu;
import java.util.Optional;

/** Input port for querying the current active day menu. */
public interface GetCurrentDayMenuUseCase {

  /**
   * Returns the currently active day menu, if any.
   *
   * @return Optional containing the active DayMenu, or empty if none is configured
   */
  Optional<DayMenu> getCurrent();
}
