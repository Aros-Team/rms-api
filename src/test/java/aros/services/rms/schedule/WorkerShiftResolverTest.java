/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import aros.services.rms.core.schedule.application.service.WorkerShiftResolver;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link WorkerShiftResolver}.
 *
 * <p><b>Feature:</b> Shift resolution in the business timezone
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Resolve An Active Shift In The Business Timezone
 *   <li>should Treat The End Time As Exclusive
 *   <li>should Flatten Shifts From All Assignments
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks schedule and assignment repositories
 *   <li>Evaluates fixed instants against a Monday shift
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class WorkerShiftResolverTest {

  private static final ZoneId ZONE = ZoneId.of("America/Bogota");

  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private ScheduleRepositoryPort scheduleRepository;

  private WorkerShiftResolver resolver;
  private final UserId workerId = UserId.of(1L);
  private final ScheduleShift shift =
      new ScheduleShift(10L, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

  @BeforeEach
  void setUp() {
    resolver = new WorkerShiftResolver(assignmentRepository, scheduleRepository, ZONE);
  }

  @Test
  void shouldResolveAnActiveShiftInTheBusinessTimezone() {
    // 13:00Z is 08:00 in America/Bogota on Monday 2026-11-23.
    assertTrue(resolver.isWithinShift(List.of(shift), Instant.parse("2026-11-23T13:00:00Z")));
  }

  @Test
  void shouldTreatTheEndTimeAsExclusive() {
    // 17:00Z is 12:00 local, the exclusive end of the shift.
    assertFalse(resolver.isWithinShift(List.of(shift), Instant.parse("2026-11-23T17:00:00Z")));
  }

  @Test
  void shouldRejectAnotherWeekday() {
    assertFalse(resolver.isWithinShift(List.of(shift), Instant.parse("2026-11-24T13:00:00Z")));
  }

  @Test
  void shouldFlattenShiftsFromAllAssignments() {
    var schedule =
        new Schedule(
            ScheduleId.of(1L),
            "Morning",
            "Morning",
            List.of(
                shift,
                new ScheduleShift(
                    11L, DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    when(assignmentRepository.findByWorkerId(workerId))
        .thenReturn(List.of(new WorkerScheduleAssignment(workerId, ScheduleId.of(1L))));
    when(scheduleRepository.findById(ScheduleId.of(1L))).thenReturn(Optional.of(schedule));

    assertEquals(2, resolver.shiftsFor(workerId).size());
    assertEquals(
        Optional.empty(), resolver.activeShift(workerId, Instant.parse("2026-11-23T18:00:00Z")));
  }
}
