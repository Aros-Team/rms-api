/* (C) 2026 */

package aros.services.rms.core.daymenu.port.output;

import aros.services.rms.core.daymenu.domain.DayMenu;
import java.util.Optional;

/** Output port for DayMenu persistence operations. */
public interface DayMenuRepositoryPort {

  /**
   * Returns the currently active day menu record, if any.
   *
   * @return Optional containing the active DayMenu, or empty if none exists
   */
  Optional<DayMenu> findActive();

  /**
   * Persists a new active day menu record.
   *
   * @param dayMenu the day menu to save
   * @return the saved DayMenu with generated id
   */
  DayMenu save(DayMenu dayMenu);

  /** Deletes the currently active day menu record. No-op if none exists. */
  void deleteActive();
}
