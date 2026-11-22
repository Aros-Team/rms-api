/* (C) 2026 */

package aros.services.rms.core.schedule.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * Rules for pairing clock entries into attendance sessions.
 *
 * <p>A session is open when the latest entry for a worker is an {@link LogType#IN} that is still
 * recent. Entries older than {@link #MAX_OPEN_SESSION} are treated as abandoned so a forgotten
 * clock-out does not block the worker forever; there is no correction endpoint yet.
 */
public final class TimeLogSessions {

  /** Maximum age of a clock-in that is still considered an open session. */
  public static final Duration MAX_OPEN_SESSION = Duration.ofHours(16);

  private TimeLogSessions() {}

  /**
   * Returns whether the given entry leaves an open clock-in session at {@code now}.
   *
   * @param latest the most recent entry for the worker, or null when there is none
   * @param now the current instant
   * @return true when the latest entry is a recent clock-in without a matching clock-out
   */
  public static boolean isOpen(TimeLog latest, Instant now) {
    if (latest == null || latest.getType() != LogType.IN || latest.getTimestamp() == null) {
      return false;
    }
    return !latest.getTimestamp().isBefore(now.minus(MAX_OPEN_SESSION));
  }
}
