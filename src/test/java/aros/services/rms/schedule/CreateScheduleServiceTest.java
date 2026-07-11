/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.application.service.CreateScheduleService;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.input.CreateScheduleUseCase.CreateScheduleInfo;
import aros.services.rms.core.schedule.port.input.CreateScheduleUseCase.CreateScheduleInfo.ShiftInfo;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateScheduleServiceTest {

  @Mock private ScheduleRepositoryPort scheduleRepository;
  @Mock private Logger logger;

  private CreateScheduleService service;

  @BeforeEach
  void setUp() {
    service = new CreateScheduleService(scheduleRepository, logger);
  }

  @Test
  void shouldCreateScheduleSuccessfully() {
    var info =
        new CreateScheduleInfo(
            "Morning", "Morning shift", List.of(new ShiftInfo("MONDAY", "08:00", "12:00")));

    var saved =
        new Schedule(
            ScheduleId.of(1L),
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));

    when(scheduleRepository.existsByName("Morning")).thenReturn(false);
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(saved);

    Schedule result = service.create(info);

    assertNotNull(result);
    assertEquals(1L, result.getId().value());
    assertEquals("Morning", result.getName());
    verify(scheduleRepository).save(any(Schedule.class));
  }

  @Test
  void shouldThrowWhenNameAlreadyExists() {
    var info =
        new CreateScheduleInfo(
            "Morning", "Morning shift", List.of(new ShiftInfo("MONDAY", "08:00", "12:00")));

    when(scheduleRepository.existsByName("Morning")).thenReturn(true);

    assertThrows(ScheduleAlreadyExistsException.class, () -> service.create(info));
  }

  @Test
  void shouldThrowWhenShiftsHaveOverlap() {
    var info =
        new CreateScheduleInfo(
            "Bad",
            "Overlapping shifts",
            List.of(
                new ShiftInfo("MONDAY", "08:00", "12:00"),
                new ShiftInfo("MONDAY", "09:00", "13:00")));

    when(scheduleRepository.existsByName("Bad")).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> service.create(info));
  }
}
