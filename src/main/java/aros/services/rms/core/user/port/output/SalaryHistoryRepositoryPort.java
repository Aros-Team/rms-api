/* (C) 2026 */

package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import aros.services.rms.core.user.domain.UserId;
import java.util.List;

/** Output port for salary history persistence operations. */
public interface SalaryHistoryRepositoryPort {

  /**
   * Saves a salary history entry.
   *
   * @param entry the entry to save
   * @return the saved entry
   */
  SalaryHistoryEntry save(SalaryHistoryEntry entry);

  /**
   * Finds all salary history entries for a user.
   *
   * @param userId the user identifier
   * @return list of salary history entries ordered by most recent first
   */
  List<SalaryHistoryEntry> findByUserId(UserId userId);
}
