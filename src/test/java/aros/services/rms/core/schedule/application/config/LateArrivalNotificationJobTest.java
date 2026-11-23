/* (C) 2026 */

package aros.services.rms.core.schedule.application.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationSeverity;
import aros.services.rms.core.notification.domain.port.input.NotificationUseCase;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessTimeZoneProvider;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link LateArrivalNotificationJob}.
 *
 * <p><b>Feature:</b> notify the admin when a scheduled worker is 10 minutes late.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>a shift that started 10+ minutes ago with no clock-in produces a WARNING notification
 *   <li>a worker with an IN entry for the shift produces nothing
 *   <li>nothing is published before the grace period elapses
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class LateArrivalNotificationJobTest {

  private static final UserId WORKER_ID = UserId.of(7L);
  private static final ScheduleId SCHEDULE_ID = ScheduleId.of(1L);
  private static final long SHIFT_ID = 11L;
  private static final Schedule SHIFT_SCHEDULE =
      new Schedule(
          SCHEDULE_ID,
          "Mañana",
          null,
          List.of(
              new ScheduleShift(
                  SHIFT_ID, DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(17, 0))));

  @Mock private ScheduleRepositoryPort scheduleRepository;
  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private TimeLogRepositoryPort timeLogRepository;
  @Mock private UserRepositoryPort userRepository;
  @Mock private NotificationUseCase notificationUseCase;
  @Mock private BusinessTimeZoneProvider timeZoneProvider;

  @BeforeEach
  void setUp() {
    when(timeZoneProvider.getZoneId()).thenReturn(ZoneOffset.UTC);
    when(scheduleRepository.findAll()).thenReturn(List.of(SHIFT_SCHEDULE));
  }

  private void assignWorkerToShift() {
    when(assignmentRepository.findByScheduleId(SCHEDULE_ID))
        .thenReturn(
            List.of(
                new WorkerScheduleAssignment(
                    WorkerScheduleAssignmentId.of(1L), WORKER_ID, SCHEDULE_ID, Instant.EPOCH)));
  }

  private LateArrivalNotificationJob jobAt(String instant) {
    return new LateArrivalNotificationJob(
        scheduleRepository,
        assignmentRepository,
        timeLogRepository,
        userRepository,
        notificationUseCase,
        timeZoneProvider,
        Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
  }

  @Test
  void shouldNotifyWhenTheShiftStartedTenMinutesAgoWithoutClockIn() {
    assignWorkerToShift();
    when(timeLogRepository.findByWorkerIdAndDateRange(eq(WORKER_ID), any(), any()))
        .thenReturn(List.of());
    User worker = mock(User.class);
    when(worker.getName()).thenReturn("Ana Pérez");
    when(userRepository.findById(WORKER_ID)).thenReturn(Optional.of(worker));

    jobAt("2026-11-23T13:11:00Z").checkLateArrivals();

    ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
    verify(notificationUseCase).create(captor.capture());
    NotificationCommand command = captor.getValue();
    assertEquals("WORKER_LATE", command.code());
    assertEquals(NotificationSeverity.WARNING, command.severity());
    assertTrue(command.message().contains("Ana Pérez"));
    assertTrue(command.message().contains("13:00"));
    assertTrue(command.dedupeKey().contains("2026-11-23"));
  }

  @Test
  void shouldSkipWhenTheWorkerAlreadyClockedIn() {
    assignWorkerToShift();
    when(timeLogRepository.findByWorkerIdAndDateRange(eq(WORKER_ID), any(), any()))
        .thenReturn(
            List.of(new TimeLog(WORKER_ID, Instant.parse("2026-11-23T12:58:00Z"), true, SHIFT_ID)));

    jobAt("2026-11-23T13:11:00Z").checkLateArrivals();

    verifyNoInteractions(notificationUseCase);
  }

  @Test
  void shouldWaitUntilTheGracePeriodElapses() {
    jobAt("2026-11-23T13:05:00Z").checkLateArrivals();

    verifyNoInteractions(notificationUseCase);
  }
}
