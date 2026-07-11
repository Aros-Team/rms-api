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
import aros.services.rms.core.schedule.application.service.UpdateScheduleService;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase.UpdateScheduleInfo;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase.UpdateScheduleInfo.ShiftInfo;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateScheduleServiceTest {

  @Mock private ScheduleRepositoryPort scheduleRepository;
  @Mock private Logger logger;

  private UpdateScheduleService service;

  @BeforeEach
  void setUp() {
    service = new UpdateScheduleService(scheduleRepository, logger);
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
}
