package aros.services.rms.core.specialselection.domain;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single weekly availability window for a special selection, defined by day of week
 * and start/end times.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionScheduleEntry {
  private Long id;
  private Long productId;
  private DayOfWeek dayOfWeek;
  private LocalTime startTime;
  private LocalTime endTime;
}
