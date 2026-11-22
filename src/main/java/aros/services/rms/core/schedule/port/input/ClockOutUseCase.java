/* (C) 2026 */

package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.user.domain.UserId;

/** Use case for an explicit clock-out by the authenticated worker. */
public interface ClockOutUseCase {
  /**
   * Closes the worker's open attendance session.
   *
   * @param workerId the worker who clocks out
   * @return the created clock-out entry
   */
  TimeLog clockOut(UserId workerId);
}
