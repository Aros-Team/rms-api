/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
import aros.services.rms.core.schedule.application.exception.WeeklyHoursExceededException;
import aros.services.rms.core.schedule.application.service.UpdateScheduleService;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase.UpdateScheduleInfo;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase.UpdateScheduleInfo.ShiftInfo;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link UpdateScheduleService}.
 *
 * <p><b>Feature:</b> Schedule update operations
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>set Up
 *   <li>should Update Schedule Successfully
 *   <li>should Throw When Schedule Not Found
 *   <li>should Throw When Name Already Exists
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks ScheduleRepositoryPort, Logger dependencies via Mockito
 *   <li>Verifies business logic with unit-level assertions
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UpdateScheduleServiceTest {

  @Mock private ScheduleRepositoryPort scheduleRepository;
  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private Logger logger;

  private UpdateScheduleService service;

  @BeforeEach
  void setUp() {
    service = new UpdateScheduleService(scheduleRepository, assignmentRepository, logger);
  }

  @Test
  void shouldUpdateScheduleSuccessfully() {
    var id = ScheduleId.of(1L);
    var existing =
        new Schedule(
            id,
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    var info =
        new UpdateScheduleInfo(
            "Evening", "Evening shift", List.of(new ShiftInfo("MONDAY", "14:00", "18:00")));

    when(scheduleRepository.findById(id)).thenReturn(Optional.of(existing));
    when(scheduleRepository.save(any(Schedule.class))).thenAnswer(i -> i.getArgument(0));

    var result = service.update(id, info);

    assertEquals("Evening", result.getName());
    assertEquals("Evening shift", result.getDescription());
    verify(scheduleRepository).save(existing);
  }

  @Test
  void shouldThrowWhenScheduleNotFound() {
    var id = ScheduleId.of(99L);
    var info =
        new UpdateScheduleInfo(
            "Evening", "Evening shift", List.of(new ShiftInfo("MONDAY", "14:00", "18:00")));

    when(scheduleRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ScheduleNotFoundException.class, () -> service.update(id, info));
  }

  @Test
  void shouldThrowWhenNameAlreadyExists() {
    var id = ScheduleId.of(1L);
    var existing =
        new Schedule(
            id,
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    var info =
        new UpdateScheduleInfo(
            "Evening", "Evening shift", List.of(new ShiftInfo("MONDAY", "14:00", "18:00")));

    when(scheduleRepository.findById(id)).thenReturn(Optional.of(existing));
    when(scheduleRepository.existsByName("Evening")).thenReturn(true);

    assertThrows(ScheduleAlreadyExistsException.class, () -> service.update(id, info));
  }

  @Test
  void shouldRejectUpdateThatOverlapsAnotherScheduleOfAnAssignedWorker() {
    var id = ScheduleId.of(1L);
    var otherId = ScheduleId.of(2L);
    var workerId = UserId.of(100L);
    var existing =
        new Schedule(
            id,
            "Morning",
            null,
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    var other =
        new Schedule(
            otherId,
            "Late",
            null,
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(14, 0))));
    final var info =
        new UpdateScheduleInfo("Morning", null, List.of(new ShiftInfo("MONDAY", "09:00", "11:00")));
    var assignment = new WorkerScheduleAssignment(workerId, id);
    var otherAssignment = new WorkerScheduleAssignment(workerId, otherId);

    when(scheduleRepository.findById(id)).thenReturn(Optional.of(existing));
    when(scheduleRepository.findById(otherId)).thenReturn(Optional.of(other));
    when(assignmentRepository.findByScheduleId(id)).thenReturn(List.of(assignment));
    when(assignmentRepository.findByWorkerId(workerId))
        .thenReturn(List.of(assignment, otherAssignment));

    assertThrows(ShiftOverlapException.class, () -> service.update(id, info));
  }

  @Test
  void shouldRejectUpdateThatPushesAnAssignedWorkerOverTheWeeklyCap() {
    var id = ScheduleId.of(1L);
    var otherId = ScheduleId.of(2L);
    var workerId = UserId.of(100L);
    var existing =
        new Schedule(
            id,
            "Weekdays",
            null,
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    var other =
        new Schedule(
            otherId,
            "Sunday",
            null,
            List.of(new ScheduleShift(DayOfWeek.SUNDAY, LocalTime.of(8, 0), LocalTime.of(16, 0))));
    final var info =
        new UpdateScheduleInfo(
            "Weekdays",
            null,
            List.of(
                new ShiftInfo("MONDAY", "08:00", "16:00"),
                new ShiftInfo("TUESDAY", "08:00", "16:00"),
                new ShiftInfo("WEDNESDAY", "08:00", "16:00"),
                new ShiftInfo("THURSDAY", "08:00", "16:00"),
                new ShiftInfo("FRIDAY", "08:00", "16:00")));
    var assignment = new WorkerScheduleAssignment(workerId, id);
    var otherAssignment = new WorkerScheduleAssignment(workerId, otherId);

    when(scheduleRepository.findById(id)).thenReturn(Optional.of(existing));
    when(scheduleRepository.findById(otherId)).thenReturn(Optional.of(other));
    when(assignmentRepository.findByScheduleId(id)).thenReturn(List.of(assignment));
    when(assignmentRepository.findByWorkerId(workerId))
        .thenReturn(List.of(assignment, otherAssignment));

    assertThrows(WeeklyHoursExceededException.class, () -> service.update(id, info));
  }
}
