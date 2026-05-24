/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleHasAssignmentsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.service.DeleteScheduleService;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteScheduleServiceTest {

  @Mock private ScheduleRepositoryPort scheduleRepository;
  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private Logger logger;

  private DeleteScheduleService service;

  @BeforeEach
  void setUp() {
    service = new DeleteScheduleService(scheduleRepository, assignmentRepository, logger);
  }

  @Test
  void shouldDeleteScheduleSuccessfully() {
    var id = ScheduleId.of(1L);
    var schedule =
        new Schedule(
            id,
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));

    when(scheduleRepository.findById(id)).thenReturn(Optional.of(schedule));
    when(assignmentRepository.existsByScheduleId(id)).thenReturn(false);

    service.delete(id);

    verify(scheduleRepository).delete(id);
  }

  @Test
  void shouldThrowWhenScheduleNotFound() {
    var id = ScheduleId.of(99L);

    when(scheduleRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ScheduleNotFoundException.class, () -> service.delete(id));
  }

  @Test
  void shouldThrowWhenScheduleHasAssignments() {
    var id = ScheduleId.of(1L);
    var schedule =
        new Schedule(
            id,
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));

    when(scheduleRepository.findById(id)).thenReturn(Optional.of(schedule));
    when(assignmentRepository.existsByScheduleId(id)).thenReturn(true);

    assertThrows(ScheduleHasAssignmentsException.class, () -> service.delete(id));
  }
}
