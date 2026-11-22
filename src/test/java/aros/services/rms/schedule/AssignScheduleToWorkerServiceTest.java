/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyAssignedException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
import aros.services.rms.core.schedule.application.exception.WeeklyHoursExceededException;
import aros.services.rms.core.schedule.application.service.AssignScheduleToWorkerService;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase.AssignInfo;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link AssignScheduleToWorkerService}.
 *
 * <p><b>Feature:</b> Schedule assignment to workers
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Assign Schedule Successfully
 *   <li>should Throw When Schedule Not Found
 *   <li>should Throw When Shift Overlaps
 *   <li>should Reject A Duplicate Assignment
 *   <li>should Warn Above The Legal Weekly Reference
 *   <li>should Reject Above The Hard Weekly Cap
 *   <li>should Reject A Cumulative Assignment Above The Hard Cap
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the schedule and assignment repositories
 *   <li>Verifies weekly hour arithmetic and conflict rules
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AssignScheduleToWorkerServiceTest {

  @Mock private ScheduleRepositoryPort scheduleRepository;
  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private Logger logger;

  private AssignScheduleToWorkerService service;

  @BeforeEach
  void setUp() {
    service = new AssignScheduleToWorkerService(scheduleRepository, assignmentRepository, logger);
  }

  @Test
  void shouldAssignScheduleSuccessfully() {
    var workerId = UserId.of(1L);
    var scheduleId = ScheduleId.of(1L);
    var info = new AssignInfo(workerId, 1L);

    var schedule = schedule(scheduleId, "Morning", 8, 12);
    var assignment = new WorkerScheduleAssignment(workerId, scheduleId);

    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of());
    when(assignmentRepository.save(any(WorkerScheduleAssignment.class))).thenReturn(assignment);

    var result = service.assign(info);

    assertNotNull(result.assignment());
    assertEquals(4.0, result.weeklyHours());
    assertNull(result.warning());
    verify(assignmentRepository).save(any(WorkerScheduleAssignment.class));
  }

  @Test
  void shouldThrowWhenScheduleNotFound() {
    var info = new AssignInfo(UserId.of(1L), 99L);

    when(scheduleRepository.findById(ScheduleId.of(99L))).thenReturn(Optional.empty());

    assertThrows(ScheduleNotFoundException.class, () -> service.assign(info));
  }

  @Test
  void shouldThrowWhenShiftOverlaps() {
    var workerId = UserId.of(1L);
    var newScheduleId = ScheduleId.of(2L);
    var existingScheduleId = ScheduleId.of(1L);
    var newSchedule = schedule(newScheduleId, "Afternoon", 9, 13);
    var existingSchedule = schedule(existingScheduleId, "Morning", 8, 12);
    var existingAssignment = new WorkerScheduleAssignment(workerId, existingScheduleId);

    when(scheduleRepository.findById(newScheduleId)).thenReturn(Optional.of(newSchedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of(existingAssignment));
    when(scheduleRepository.findById(existingScheduleId)).thenReturn(Optional.of(existingSchedule));

    var info = new AssignInfo(workerId, 2L);

    assertThrows(ShiftOverlapException.class, () -> service.assign(info));
  }

  @Test
  void shouldRejectDuplicateAssignment() {
    var workerId = UserId.of(1L);
    var scheduleId = ScheduleId.of(1L);
    var schedule = schedule(scheduleId, "Morning", 8, 12);
    var existingAssignment = new WorkerScheduleAssignment(workerId, scheduleId);

    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of(existingAssignment));

    assertThrows(
        ScheduleAlreadyAssignedException.class, () -> service.assign(new AssignInfo(workerId, 1L)));
  }

  @Test
  void shouldWarnAboveTheLegalWeeklyReference() {
    var workerId = UserId.of(1L);
    var scheduleId = ScheduleId.of(1L);
    var schedule = fullWeek(scheduleId, 9, 18);
    var assignment = new WorkerScheduleAssignment(workerId, scheduleId);

    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of());
    when(assignmentRepository.save(any(WorkerScheduleAssignment.class))).thenReturn(assignment);

    var result = service.assign(new AssignInfo(workerId, 1L));

    assertEquals(63.0, result.weeklyHours());
    assertNotNull(result.warning());
    assertTrue(result.warning().contains("48"));
  }

  @Test
  void shouldRejectAboveTheHardWeeklyCap() {
    var workerId = UserId.of(1L);
    var scheduleId = ScheduleId.of(1L);
    var schedule = fullWeek(scheduleId, 10, 20);

    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of());

    var exception =
        assertThrows(
            WeeklyHoursExceededException.class, () -> service.assign(new AssignInfo(workerId, 1L)));
    assertEquals(70.0, exception.getWeeklyHours());
    assertEquals(66, exception.getHardWeeklyCapHours());
  }

  @Test
  void shouldRejectCumulativeAssignmentAboveHardCap() {
    var workerId = UserId.of(1L);
    var existingScheduleId = ScheduleId.of(1L);
    var newScheduleId = ScheduleId.of(2L);
    var existingSchedule = fullWeek(existingScheduleId, 8, 13);
    var newSchedule = fullWeek(newScheduleId, 14, 19);
    var existingAssignment = new WorkerScheduleAssignment(workerId, existingScheduleId);

    when(scheduleRepository.findById(newScheduleId)).thenReturn(Optional.of(newSchedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of(existingAssignment));
    when(scheduleRepository.findById(existingScheduleId)).thenReturn(Optional.of(existingSchedule));

    assertThrows(
        WeeklyHoursExceededException.class, () -> service.assign(new AssignInfo(workerId, 2L)));
  }

  private Schedule schedule(ScheduleId id, String name, int startHour, int endHour) {
    return new Schedule(
        id,
        name,
        name,
        List.of(
            new ScheduleShift(
                DayOfWeek.MONDAY, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0))));
  }

  private Schedule fullWeek(ScheduleId id, int startHour, int endHour) {
    List<ScheduleShift> shifts = new ArrayList<>();
    for (DayOfWeek day : DayOfWeek.values()) {
      shifts.add(new ScheduleShift(day, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)));
    }
    return new Schedule(id, "Week " + id.value(), "Full week", shifts);
  }
}
