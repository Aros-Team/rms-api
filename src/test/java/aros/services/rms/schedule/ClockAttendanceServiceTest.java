/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.NoOpenSessionException;
import aros.services.rms.core.schedule.application.exception.OpenSessionAlreadyExistsException;
import aros.services.rms.core.schedule.application.service.ClockAttendanceService;
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
 * Tests for {@link ClockAttendanceService}.
 *
 * <p><b>Feature:</b> Explicit worker clock-in and clock-out
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Open A Session Inside A Shift
 *   <li>should Refuse A Duplicate Open Session
 *   <li>should Allow A New Session After A Stale Clock In
 *   <li>should Close An Open Session
 *   <li>should Refuse A Clock Out Without An Open Session
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Uses a fixed clock at Monday 08:00 in America/Bogota
 *   <li>Mocks the latest-entry lookup and the shift resolver
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ClockAttendanceServiceTest {

  private static final Instant NOW = Instant.parse("2026-11-23T13:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Mock private TimeLogRepositoryPort timeLogRepository;
  @Mock private WorkerShiftResolver shiftResolver;
  @Mock private Logger logger;

  private ClockAttendanceService service;
  private final UserId workerId = UserId.of(7L);
  private final ScheduleShift shift =
      new ScheduleShift(10L, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

  @BeforeEach
  void setUp() {
    service = new ClockAttendanceService(timeLogRepository, shiftResolver, logger, CLOCK);
  }

  @Test
  void shouldOpenSessionInsideShift() {
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.empty());
    when(shiftResolver.activeShift(workerId, NOW)).thenReturn(Optional.of(shift));
    when(timeLogRepository.save(any(TimeLog.class)))
        .thenAnswer(
            invocation -> {
              TimeLog log = invocation.getArgument(0);
              log.setId(TimeLogId.of(100L));
              return log;
            });

    TimeLog saved = service.clockIn(workerId);

    assertEquals(LogType.IN, saved.getType());
    assertTrue(saved.isWithinShift());
    assertEquals(10L, saved.getRelatedShiftId());
    assertEquals(NOW, saved.getTimestamp());
  }

  @Test
  void shouldRefuseDuplicateOpenSession() {
    var open =
        new TimeLog(TimeLogId.of(99L), workerId, NOW.minusSeconds(300), LogType.IN, true, 10L);
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.of(open));

    assertThrows(OpenSessionAlreadyExistsException.class, () -> service.clockIn(workerId));
    verify(timeLogRepository, never()).save(any(TimeLog.class));
  }

  @Test
  void shouldAllowNewSessionAfterStaleClockIn() {
    var stale =
        new TimeLog(
            TimeLogId.of(98L), workerId, NOW.minusSeconds(20 * 3600), LogType.IN, true, 10L);
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.of(stale));
    when(shiftResolver.activeShift(workerId, NOW)).thenReturn(Optional.of(shift));
    when(timeLogRepository.save(any(TimeLog.class)))
        .thenAnswer(
            invocation -> {
              TimeLog log = invocation.getArgument(0);
              log.setId(TimeLogId.of(101L));
              return log;
            });

    TimeLog saved = service.clockIn(workerId);

    assertEquals(LogType.IN, saved.getType());
  }

  @Test
  void shouldCloseAnOpenSession() {
    var open =
        new TimeLog(TimeLogId.of(99L), workerId, NOW.minusSeconds(300), LogType.IN, true, 10L);
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.of(open));
    when(shiftResolver.activeShift(workerId, NOW)).thenReturn(Optional.of(shift));
    when(timeLogRepository.save(any(TimeLog.class)))
        .thenAnswer(
            invocation -> {
              TimeLog log = invocation.getArgument(0);
              log.setId(TimeLogId.of(102L));
              return log;
            });

    TimeLog saved = service.clockOut(workerId);

    assertEquals(LogType.OUT, saved.getType());
    assertTrue(saved.isWithinShift());
    assertEquals(10L, saved.getRelatedShiftId());
  }

  @Test
  void shouldRefuseClockOutWithoutOpenSession() {
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.empty());

    assertThrows(NoOpenSessionException.class, () -> service.clockOut(workerId));
    verify(timeLogRepository, never()).save(any(TimeLog.class));
  }

  @Test
  void shouldRefuseClockOutAfterClosedSession() {
    var closed =
        new TimeLog(TimeLogId.of(99L), workerId, NOW.minusSeconds(300), LogType.OUT, true, 10L);
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.of(closed));

    assertThrows(NoOpenSessionException.class, () -> service.clockOut(workerId));
  }

  @Test
  void shouldRecordOffShiftClockInWithoutShiftReference() {
    when(timeLogRepository.findLatestByWorkerId(workerId)).thenReturn(Optional.empty());
    when(shiftResolver.activeShift(workerId, NOW)).thenReturn(Optional.empty());
    when(timeLogRepository.save(any(TimeLog.class)))
        .thenAnswer(
            invocation -> {
              TimeLog log = invocation.getArgument(0);
              log.setId(TimeLogId.of(103L));
              return log;
            });

    TimeLog saved = service.clockIn(workerId);

    assertFalse(saved.isWithinShift());
    assertNull(saved.getRelatedShiftId());
  }
}
