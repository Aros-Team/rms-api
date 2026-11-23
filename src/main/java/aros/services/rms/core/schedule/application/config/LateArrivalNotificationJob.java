/* (C) 2026 */

package aros.services.rms.core.schedule.application.config;

import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationSeverity;
import aros.services.rms.core.notification.domain.port.input.NotificationUseCase;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessTimeZoneProvider;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Raises a notification when a scheduled worker is 10 minutes late.
 *
 * <p>Runs every minute. A notification fires only for shifts whose start is at least {@code
 * GRACE_MINUTES} in the past and whose end has not passed yet, and only when no IN entry exists for
 * that worker around the shift. Deduplication is per worker, shift and business date, so the job
 * can run repeatedly without producing duplicates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LateArrivalNotificationJob {

  static final int GRACE_MINUTES = 10;
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
  private static final String ATTENDANCE_ROUTE = "/admin/schedules?tab=registros";

  private final ScheduleRepositoryPort scheduleRepository;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final TimeLogRepositoryPort timeLogRepository;
  private final UserRepositoryPort userRepository;
  private final NotificationUseCase notificationUseCase;
  private final BusinessTimeZoneProvider timeZoneProvider;
  private final Clock clock;

  /** Checks every active shift for a missing clock-in after the grace period. */
  @Scheduled(cron = "0 * * * * *")
  @SchedulerLock(
      name = "lateArrivalNotifications",
      lockAtLeastFor = "PT30S",
      lockAtMostFor = "PT2M")
  public void checkLateArrivals() {
    Instant now = clock.instant();
    ZoneId zone = timeZoneProvider.getZoneId();
    ZonedDateTime localNow = now.atZone(zone);
    DayOfWeek today = DayOfWeek.valueOf(localNow.getDayOfWeek().name());
    LocalTime localTime = localNow.toLocalTime();

    for (Schedule schedule : scheduleRepository.findAll()) {
      for (ScheduleShift shift : schedule.getShiftsForDay(today)) {
        LocalTime graceEnd = shift.startTime().plusMinutes(GRACE_MINUTES);
        if (localTime.isBefore(graceEnd) || !localTime.isBefore(shift.endTime())) {
          continue;
        }
        for (WorkerScheduleAssignment assignment :
            assignmentRepository.findByScheduleId(schedule.getId())) {
          notifyIfLate(assignment.getWorkerId(), shift, localNow);
        }
      }
    }
  }

  private void notifyIfLate(UserId workerId, ScheduleShift shift, ZonedDateTime localNow) {
    try {
      if (hasClockedIn(workerId, shift, localNow)) {
        return;
      }

      String workerName =
          userRepository.findById(workerId).map(user -> user.getName()).orElse("Un trabajador");
      String start = shift.startTime().format(TIME_FORMAT);

      notificationUseCase.create(
          new NotificationCommand(
              "WORKER_LATE",
              NotificationSeverity.WARNING,
              "Llegada tarde",
              workerName + " aún no ha marcado entrada. Su turno inició a las " + start + ".",
              ATTENDANCE_ROUTE,
              "LATE:" + workerId.value() + ":" + shift.id() + ":" + localNow.toLocalDate()));
    } catch (Exception exception) {
      log.error(
          "Failed to publish late-arrival notification for worker {} shift {}: {}",
          workerId.value(),
          shift.id(),
          exception.getMessage(),
          exception);
    }
  }

  private boolean hasClockedIn(UserId workerId, ScheduleShift shift, ZonedDateTime localNow) {
    ZonedDateTime shiftStart =
        localNow.toLocalDate().atTime(shift.startTime()).atZone(localNow.getZone());
    ZonedDateTime shiftEnd =
        localNow.toLocalDate().atTime(shift.endTime()).atZone(localNow.getZone());
    Instant from = shiftStart.minusHours(2).toInstant();
    Instant to = shiftEnd.toInstant();

    return timeLogRepository.findByWorkerIdAndDateRange(workerId, from, to).stream()
        .anyMatch(entry -> entry.getType() == LogType.IN);
  }
}
