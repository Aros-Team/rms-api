package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Determines whether special selections are available at a given moment, based on their active flag
 * and optional weekly schedule.
 */
public class SpecialSelectionAvailabilityService {

  /**
   * Checks whether the given configuration is available at the provided date and time.
   *
   * @param config the special selection configuration
   * @param dateTime the date and time to check
   * @return true if the configuration is active and within its schedule window
   */
  public boolean isAvailable(SpecialSelectionConfiguration config, LocalDateTime dateTime) {
    if (!config.isActive()) {
      return false;
    }
    if (!config.isSchedulingRequired()
        || config.getSchedule() == null
        || config.getSchedule().isEmpty()) {
      return true;
    }
    java.time.DayOfWeek javaDay = dateTime.getDayOfWeek();
    DayOfWeek scheduleDay = mapToScheduleDay(javaDay);
    LocalTime time = dateTime.toLocalTime();

    return config.getSchedule().stream()
        .anyMatch(
            entry ->
                entry.getDayOfWeek() == scheduleDay
                    && !time.isBefore(entry.getStartTime())
                    && (entry.getEndTime() == null || !time.isAfter(entry.getEndTime())));
  }

  /**
   * Filters the given configurations to those available at the provided date and time.
   *
   * @param configs the configurations to filter
   * @param dateTime the date and time to check
   * @return list of configurations available at the given moment
   */
  public List<SpecialSelectionConfiguration> filterAvailable(
      List<SpecialSelectionConfiguration> configs, LocalDateTime dateTime) {
    return configs.stream().filter(c -> isAvailable(c, dateTime)).collect(Collectors.toList());
  }

  private DayOfWeek mapToScheduleDay(java.time.DayOfWeek javaDay) {
    return switch (javaDay) {
      case MONDAY -> DayOfWeek.MONDAY;
      case TUESDAY -> DayOfWeek.TUESDAY;
      case WEDNESDAY -> DayOfWeek.WEDNESDAY;
      case THURSDAY -> DayOfWeek.THURSDAY;
      case FRIDAY -> DayOfWeek.FRIDAY;
      case SATURDAY -> DayOfWeek.SATURDAY;
      case SUNDAY -> DayOfWeek.SUNDAY;
    };
  }
}
