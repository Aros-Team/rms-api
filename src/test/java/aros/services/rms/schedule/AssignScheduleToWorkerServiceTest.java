/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    var schedule =
        new Schedule(
            scheduleId,
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    var assignment = new WorkerScheduleAssignment(workerId, scheduleId);

    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of());
    when(assignmentRepository.save(any(WorkerScheduleAssignment.class))).thenReturn(assignment);

    var result = service.assign(info);

    assertNotNull(result);
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
    var info = new AssignInfo(workerId, 2L);

    var newSchedule =
        new Schedule(
            newScheduleId,
            "Afternoon",
            "Afternoon shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0))));
    var existingSchedule =
        new Schedule(
            existingScheduleId,
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));

    var existingAssignment = new WorkerScheduleAssignment(workerId, existingScheduleId);

    when(scheduleRepository.findById(newScheduleId)).thenReturn(Optional.of(newSchedule));
    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of(existingAssignment));
    when(scheduleRepository.findById(existingScheduleId)).thenReturn(Optional.of(existingSchedule));

    assertThrows(ShiftOverlapException.class, () -> service.assign(info));
  }
}
