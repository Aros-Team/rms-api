/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

/** Opening window for one weekday. */
@Data
@Builder
public class OpeningHoursDay {
  private DayOfWeek dayOfWeek;
  private boolean closed;

  /** Opening time when the day is open; {@code null} when closed. */
  private LocalTime openTime;

  /** Closing time when the day is open; {@code null} when closed. */
  private LocalTime closeTime;
}
