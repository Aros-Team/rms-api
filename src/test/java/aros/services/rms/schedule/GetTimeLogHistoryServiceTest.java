/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import aros.services.rms.core.schedule.application.service.GetTimeLogHistoryService;
import aros.services.rms.core.schedule.application.service.WorkerShiftResolver;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogId;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase.TimeLogFilter;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link GetTimeLogHistoryService}.
 *
 * <p><b>Feature:</b> Time log history retrieval
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Return History For Worker
 *   <li>should Return All History when No Worker Filter
 *   <li>should Keep Only Entries Inside A Shift when withinShift Is True
 *   <li>should Keep Only Entries Outside A Shift when withinShift Is False
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the repository and the shift resolver
 *   <li>Verifies the filter decision per entry
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class GetTimeLogHistoryServiceTest {

  @Mock private TimeLogRepositoryPort timeLogRepository;
  @Mock private WorkerShiftResolver shiftResolver;

  private GetTimeLogHistoryService service;

  @BeforeEach
  void setUp() {
    service = new GetTimeLogHistoryService(timeLogRepository, shiftResolver);
  }

  @Test
  void shouldReturnHistoryForWorker() {
    var workerId = UserId.of(1L);
    var filter = new TimeLogFilter(workerId, null, null, null);
    var log = new TimeLog(TimeLogId.of(1L), workerId, Instant.now(), LogType.IN, true, 1L);

    when(timeLogRepository.findByWorkerIdAndDateRange(eq(workerId), any(), any()))
        .thenReturn(List.of(log));

    var result = service.getHistory(filter);

    assertEquals(1, result.size());
  }

  @Test
  void shouldReturnAllHistory_whenNoWorkerFilter() {
    var filter = new TimeLogFilter(null, null, null, null);

    when(timeLogRepository.findAllByDateRange(any(), any())).thenReturn(List.of());

    var result = service.getHistory(filter);

    assertEquals(0, result.size());
  }

  @Test
  void shouldKeepOnlyEntriesInsideShiftWhenWithinShiftIsTrue() {
    var workerId = UserId.of(1L);
    var inside =
        new TimeLog(
            TimeLogId.of(1L),
            workerId,
            Instant.parse("2026-11-23T13:00:00Z"),
            LogType.IN,
            true,
            10L);
    var outside =
        new TimeLog(
            TimeLogId.of(2L),
            workerId,
            Instant.parse("2026-11-23T23:00:00Z"),
            LogType.IN,
            false,
            null);
    var shifts =
        List.of(new ScheduleShift(10L, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));

    when(timeLogRepository.findAllByDateRange(any(), any())).thenReturn(List.of(inside, outside));
    when(shiftResolver.shiftsFor(workerId)).thenReturn(shifts);
    when(shiftResolver.isWithinShift(shifts, inside.getTimestamp())).thenReturn(true);
    when(shiftResolver.isWithinShift(shifts, outside.getTimestamp())).thenReturn(false);

    var result = service.getHistory(new TimeLogFilter(null, null, null, true));

    assertEquals(1, result.size());
    assertEquals(TimeLogId.of(1L), result.get(0).getId());
  }

  @Test
  void shouldKeepOnlyEntriesOutsideShiftWhenWithinShiftIsFalse() {
    var workerId = UserId.of(1L);
    var inside =
        new TimeLog(
            TimeLogId.of(1L),
            workerId,
            Instant.parse("2026-11-23T13:00:00Z"),
            LogType.IN,
            true,
            10L);
    var outside =
        new TimeLog(
            TimeLogId.of(2L),
            workerId,
            Instant.parse("2026-11-23T23:00:00Z"),
            LogType.IN,
            false,
            null);
    var shifts =
        List.of(new ScheduleShift(10L, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));

    when(timeLogRepository.findAllByDateRange(any(), any())).thenReturn(List.of(inside, outside));
    when(shiftResolver.shiftsFor(workerId)).thenReturn(shifts);
    when(shiftResolver.isWithinShift(shifts, inside.getTimestamp())).thenReturn(true);
    when(shiftResolver.isWithinShift(shifts, outside.getTimestamp())).thenReturn(false);

    var result = service.getHistory(new TimeLogFilter(null, null, null, false));

    assertEquals(1, result.size());
    assertEquals(TimeLogId.of(2L), result.get(0).getId());
  }
}
