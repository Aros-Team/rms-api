/* (C) 2026 */

package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import java.util.List;

/** Input port for retrieving salary history. */
public interface GetSalaryHistoryUseCase {

  /**
   * Retrieves the salary history for a user.
   *
   * @param userId the user identifier
   * @return list of salary history entries ordered by most recent first
   */
  List<SalaryHistoryEntry> getSalaryHistory(Long userId);
}
