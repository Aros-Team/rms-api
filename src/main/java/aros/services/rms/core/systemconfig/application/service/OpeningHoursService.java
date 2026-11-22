/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.GetOpeningHoursUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateOpeningHoursUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.OpeningHoursPort;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/** Reads and writes the weekly opening hours, enforcing one valid window per weekday. */
@RequiredArgsConstructor
public class OpeningHoursService implements GetOpeningHoursUseCase, UpdateOpeningHoursUseCase {

  private final OpeningHoursPort port;

  /** {@inheritDoc} */
  @Override
  public List<OpeningHoursDay> get() {
    return sorted(port.findAll());
  }

  /** {@inheritDoc} */
  @Override
  public List<OpeningHoursDay> update(List<OpeningHoursDay> days) {
    validate(days);
    port.replaceAll(days);
    return sorted(port.findAll());
  }

  private List<OpeningHoursDay> sorted(List<OpeningHoursDay> days) {
    return days.stream()
        .sorted(Comparator.comparingInt(day -> day.getDayOfWeek().getValue()))
        .toList();
  }

  private void validate(List<OpeningHoursDay> days) {
    if (days == null || days.size() != DayOfWeek.values().length) {
      throw new InvalidConfigurationException(
          "Opening hours must cover the seven weekdays exactly once");
    }
    Set<DayOfWeek> seen = EnumSet.noneOf(DayOfWeek.class);
    for (OpeningHoursDay day : days) {
      if (day == null || day.getDayOfWeek() == null) {
        throw new InvalidConfigurationException("Every opening-hours day needs a weekday");
      }
      if (!seen.add(day.getDayOfWeek())) {
        throw new InvalidConfigurationException(
            "Duplicate opening-hours day: " + day.getDayOfWeek());
      }
      if (day.isClosed()) {
        continue;
      }
      if (day.getOpenTime() == null || day.getCloseTime() == null) {
        throw new InvalidConfigurationException(
            "Open day " + day.getDayOfWeek() + " needs an opening and closing time");
      }
      if (!day.getCloseTime().isAfter(day.getOpenTime())) {
        throw new InvalidConfigurationException(
            "Closing time must be after opening time on " + day.getDayOfWeek());
      }
    }
  }
}
