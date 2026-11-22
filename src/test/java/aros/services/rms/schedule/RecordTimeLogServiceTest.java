/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.schedule.application.service.RecordTimeLogService;
import aros.services.rms.core.schedule.application.service.WorkerShiftResolver;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogId;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link RecordTimeLogService}.
 *
 * <p><b>Feature:</b> Login-time attendance recording
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Save Clock In when Worker Is Inside A Shift
 *   <li>should Skip when a Session Is Already Open
 *   <li>should Not Persist an Off-Shift Login
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Uses a fixed clock and a mocked shift resolver
 *   <li>Verifies persistence decisions and the returned shift
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RecordTimeLogServiceTest {

  private static final Instant NOW = Instant.parse("2026-11-23T13:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Mock private TimeLogRepositoryPort timeLogRepository;
  @Mock private WorkerShiftResolver shiftResolver;

  private RecordTimeLogService service;
  private final UserId workerId = UserId.of(1L);
  private final ScheduleShift shift =
      new ScheduleShift(10L, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

  @BeforeEach
  void setUp() {
    service = new RecordTimeLogService(timeLogRepository, shiftResolver, CLOCK);
  }

  @Test
  void shouldSaveClockInWhenWorkerIsInsideShift() {
    when(shiftResolver.activeShift(workerId, NOW)).thenReturn(Optional.of(shift));
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.empty());

    var result = service.execute(workerId);

    assertTrue(result.withinShift());
    assertEquals(shift, result.activeShift());
    verify(timeLogRepository).save(any(TimeLog.class));
  }

  @Test
  void shouldSkipWhenSessionIsAlreadyOpen() {
    when(shiftResolver.activeShift(workerId, NOW)).thenReturn(Optional.of(shift));
    var open =
        new TimeLog(TimeLogId.of(5L), workerId, NOW.minusSeconds(600), LogType.IN, true, 10L);
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.of(open));

    var result = service.execute(workerId);

    assertTrue(result.withinShift());
    verify(timeLogRepository, never()).save(any(TimeLog.class));
  }

  @Test
  void shouldNotPersistAnOffShiftLogin() {
    when(shiftResolver.activeShift(workerId, NOW)).thenReturn(Optional.empty());

    var result = service.execute(workerId);

    assertFalse(result.withinShift());
    assertEquals(null, result.activeShift());
    verify(timeLogRepository, never()).save(any(TimeLog.class));
  }
}
