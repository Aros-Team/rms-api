/* (C) 2026 */

package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.user.domain.UserId;

/** Use case for an explicit clock-in by the authenticated worker. */
public interface ClockInUseCase {
  /**
   * Opens an attendance session for the worker.
   *
   * @param workerId the worker who clocks in
   * @return the created clock-in entry
   */
  TimeLog clockIn(UserId workerId);
}
