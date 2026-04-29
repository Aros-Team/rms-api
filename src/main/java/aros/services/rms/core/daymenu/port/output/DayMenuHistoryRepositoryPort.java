/* (C) 2026 */

package aros.services.rms.core.daymenu.port.output;

import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Output port for DayMenuHistory persistence operations. */
public interface DayMenuHistoryRepositoryPort {

  /**
   * Persists an archived day menu entry.
   *
   * @param history the history record to save
   * @return the saved DayMenuHistory with generated id
   */
  DayMenuHistory save(DayMenuHistory history);

  /**
   * Returns a paginated list of archived day menu entries ordered by validUntil descending.
   *
   * @param pageable pagination parameters
   * @return page of DayMenuHistory records
   */
  Page<DayMenuHistory> findAll(Pageable pageable);
}
