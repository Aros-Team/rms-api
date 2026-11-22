/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.output;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import java.util.List;

/** Persistence boundary for the weekly opening hours. */
public interface OpeningHoursPort {

  /**
   * Returns every stored weekday window.
   *
   * @return the stored windows
   */
  List<OpeningHoursDay> findAll();

  /**
   * Replaces every weekday window atomically.
   *
   * @param days the new windows
   */
  void replaceAll(List<OpeningHoursDay> days);
}
