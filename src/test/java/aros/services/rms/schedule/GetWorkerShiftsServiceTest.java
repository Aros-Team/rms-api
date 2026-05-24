/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import aros.services.rms.core.schedule.application.service.GetWorkerShiftsService;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
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
class GetWorkerShiftsServiceTest {

  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private ScheduleRepositoryPort scheduleRepository;

  private GetWorkerShiftsService service;

  @BeforeEach
  void setUp() {
    service = new GetWorkerShiftsService(assignmentRepository, scheduleRepository);
  }

  @Test
  void shouldReturnShiftsForWorker() {
    var workerId = UserId.of(1L);
    var scheduleId = ScheduleId.of(1L);
    var schedule =
        new Schedule(
            scheduleId,
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    var assignment = new WorkerScheduleAssignment(workerId, scheduleId);

    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of(assignment));
    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

    var result = service.getShifts(workerId);

    assertEquals(1, result.get(DayOfWeek.MONDAY).size());
    assertEquals("Morning", result.get(DayOfWeek.MONDAY).get(0).scheduleName());
  }

  @Test
  void shouldReturnEmptyShifts_whenNoAssignments() {
    var workerId = UserId.of(1L);

    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of());

    var result = service.getShifts(workerId);

    assertTrue(result.get(DayOfWeek.MONDAY).isEmpty());
  }
}
