/* (C) 2026 */

package aros.services.rms.core.daymenu.port.input;

import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Input port for querying the paginated day menu history. */
public interface GetDayMenuHistoryUseCase {

  /**
   * Returns a paginated list of archived day menu entries, ordered by validUntil descending.
   *
   * @param pageable pagination and sorting parameters
   * @return page of DayMenuHistory records
   */
  Page<DayMenuHistory> getHistory(Pageable pageable);
}
