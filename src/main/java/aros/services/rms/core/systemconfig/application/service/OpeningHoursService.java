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
          "El horario de atención debe cubrir los siete días de la semana exactamente una vez");
    }
    Set<DayOfWeek> seen = EnumSet.noneOf(DayOfWeek.class);
    for (OpeningHoursDay day : days) {
      if (day == null || day.getDayOfWeek() == null) {
        throw new InvalidConfigurationException(
            "Cada día del horario de atención necesita un día de la semana");
      }
      if (!seen.add(day.getDayOfWeek())) {
        throw new InvalidConfigurationException(
            "Día de horario de atención duplicado: " + day.getDayOfWeek());
      }
      if (day.isClosed()) {
        continue;
      }
      if (day.getOpenTime() == null || day.getCloseTime() == null) {
        throw new InvalidConfigurationException(
            "El día abierto " + day.getDayOfWeek() + " necesita una hora de apertura y de cierre");
      }
      if (!day.getCloseTime().isAfter(day.getOpenTime())) {
        throw new InvalidConfigurationException(
            "La hora de cierre debe ser posterior a la de apertura el " + day.getDayOfWeek());
      }
    }
  }
}
