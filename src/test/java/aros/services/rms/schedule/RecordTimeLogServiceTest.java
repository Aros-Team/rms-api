/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.schedule.application.service.RecordTimeLogService;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordTimeLogServiceTest {

  @Mock private TimeLogRepositoryPort timeLogRepository;
  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private ScheduleRepositoryPort scheduleRepository;

  private RecordTimeLogService service;
  private static final ZoneId ZONE = ZoneId.of("America/Bogota");

  @BeforeEach
  void setUp() {
    service =
        new RecordTimeLogService(timeLogRepository, assignmentRepository, scheduleRepository, ZONE);
  }

  @Test
  void shouldSaveTimeLog_whenWorkerHasAssignment() {
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

    service.execute(workerId);

    verify(timeLogRepository).save(any());
  }

  @Test
  void shouldSaveTimeLog_whenNoAssignment() {
    var workerId = UserId.of(1L);

    when(assignmentRepository.findByWorkerId(workerId)).thenReturn(List.of());

    service.execute(workerId);

    verify(timeLogRepository).save(any());
  }
}
