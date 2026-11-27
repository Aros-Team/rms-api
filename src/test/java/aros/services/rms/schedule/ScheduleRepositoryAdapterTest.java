/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.infraestructure.schedule.persistence.jpa.ScheduleEntity;
import aros.services.rms.infraestructure.schedule.persistence.jpa.ScheduleJpaRepository;
import aros.services.rms.infraestructure.schedule.persistence.jpa.ScheduleMapper;
import aros.services.rms.infraestructure.schedule.persistence.jpa.ScheduleRepositoryAdapter;
import aros.services.rms.infraestructure.schedule.persistence.jpa.ScheduleShiftEntity;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link ScheduleRepositoryAdapter}.
 *
 * <p><b>Feature:</b> schedule persistence on update.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>an edited shift reuses its existing row and keeps its id, so time-log foreign keys survive;
 *   <li>unchanged shifts are left completely untouched;
 *   <li>genuinely new shifts are added and removed shifts are dropped;
 *   <li>the update never maps a fresh entity over the managed one.
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>mocks the JPA repository and mapper;
 *   <li>calls {@code save} with an existing schedule whose times changed;
 *   <li>asserts the entity instances returned by {@code getShifts()}.
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ScheduleRepositoryAdapterTest {

  @Mock private ScheduleJpaRepository internal;
  @Mock private ScheduleMapper mapper;

  private ScheduleRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ScheduleRepositoryAdapter(internal, mapper);
  }

  @Test
  void reusesExistingShiftRowsWhenAnUpdateChangesTimes() {
    ScheduleEntity entity = entity();
    ScheduleShiftEntity tuesday = shift(11L, DayOfWeek.TUESDAY, 8, 16, entity);
    ScheduleShiftEntity wednesday = shift(12L, DayOfWeek.WEDNESDAY, 8, 16, entity);
    entity.setShifts(new ArrayList<>(List.of(tuesday, wednesday)));

    Schedule updated =
        new Schedule(
            ScheduleId.of(1L),
            "Nuevo",
            "Nueva",
            List.of(
                new ScheduleShift(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new ScheduleShift(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(16, 0))));

    when(internal.findById(1L)).thenReturn(Optional.of(entity));
    when(internal.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(updated);

    Schedule result = adapter.save(updated);

    assertSame(updated, result);
    assertEquals(2, entity.getShifts().size());
    assertSame(tuesday, entity.getShifts().get(0));
    assertEquals(11L, tuesday.getId());
    assertEquals(LocalTime.of(9, 0), tuesday.getStartTime());
    assertSame(wednesday, entity.getShifts().get(1));
    assertEquals(8, wednesday.getStartTime().getHour());
    verify(mapper, never()).toEntity(any());
  }

  @Test
  void addsNewShiftsAndKeepsUnchangedOnes() {
    ScheduleEntity entity = entity();
    ScheduleShiftEntity tuesday = shift(11L, DayOfWeek.TUESDAY, 8, 16, entity);
    entity.setShifts(new ArrayList<>(List.of(tuesday)));

    Schedule updated =
        new Schedule(
            ScheduleId.of(1L),
            "Nuevo",
            "Nueva",
            List.of(
                new ScheduleShift(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(16, 0)),
                new ScheduleShift(DayOfWeek.FRIDAY, LocalTime.of(10, 0), LocalTime.of(18, 0))));

    when(internal.findById(1L)).thenReturn(Optional.of(entity));
    when(internal.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(updated);

    adapter.save(updated);

    assertEquals(2, entity.getShifts().size());
    assertSame(tuesday, entity.getShifts().get(0));
    assertTrue(
        entity.getShifts().stream().anyMatch(shift -> shift.getDayOfWeek() == DayOfWeek.FRIDAY));
    entity.getShifts().stream()
        .filter(shift -> shift.getDayOfWeek() == DayOfWeek.FRIDAY)
        .forEach(shift -> assertNull(shift.getId()));
  }

  private static ScheduleEntity entity() {
    ScheduleEntity entity = new ScheduleEntity();
    entity.setId(1L);
    entity.setName("Viejo");
    entity.setDescription("Vieja");
    return entity;
  }

  private static ScheduleShiftEntity shift(
      Long id, DayOfWeek day, int startHour, int endHour, ScheduleEntity entity) {
    ScheduleShiftEntity shift = new ScheduleShiftEntity();
    shift.setId(id);
    shift.setDayOfWeek(day);
    shift.setStartTime(LocalTime.of(startHour, 0));
    shift.setEndTime(LocalTime.of(endHour, 0));
    shift.setSchedule(entity);
    return shift;
  }
}
