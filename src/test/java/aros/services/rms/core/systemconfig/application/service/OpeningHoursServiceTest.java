/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.output.OpeningHoursPort;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link OpeningHoursService}.
 *
 * <p><b>Feature:</b> weekly opening hours.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Returns windows sorted Monday first
 *   <li>Requires the seven weekdays exactly once
 *   <li>Requires open/close times on open days and a close after the open
 *   <li>Never persists an invalid week
 * </ul>
 *
 * <p><b>How this test works:</b> mocks the port and asserts each validation branch.
 */
@ExtendWith(MockitoExtension.class)
class OpeningHoursServiceTest {

  @Mock private OpeningHoursPort port;

  private OpeningHoursService service;

  @BeforeEach
  void setUp() {
    service = new OpeningHoursService(port);
  }

  private List<OpeningHoursDay> week() {
    List<OpeningHoursDay> days = new ArrayList<>();
    for (DayOfWeek day : DayOfWeek.values()) {
      days.add(
          OpeningHoursDay.builder()
              .dayOfWeek(day)
              .closed(day == DayOfWeek.MONDAY)
              .openTime(day == DayOfWeek.MONDAY ? null : LocalTime.of(8, 0))
              .closeTime(day == DayOfWeek.MONDAY ? null : LocalTime.of(22, 0))
              .build());
    }
    return days;
  }

  @Test
  void returnsWindowsSortedMondayFirst() {
    List<OpeningHoursDay> week = week();
    java.util.Collections.reverse(week);
    when(port.findAll()).thenReturn(week);

    List<OpeningHoursDay> result = service.get();

    assertEquals(DayOfWeek.MONDAY, result.getFirst().getDayOfWeek());
    assertEquals(DayOfWeek.SUNDAY, result.getLast().getDayOfWeek());
  }

  @Test
  void rejectsAnIncompleteWeek() {
    List<OpeningHoursDay> week = week().subList(0, 6);

    assertThrows(InvalidConfigurationException.class, () -> service.update(week));
    verify(port, never()).replaceAll(any());
  }

  @Test
  void rejectsDuplicateDays() {
    List<OpeningHoursDay> week = new ArrayList<>(week());
    week.set(1, week.get(0));

    assertThrows(InvalidConfigurationException.class, () -> service.update(week));
    verify(port, never()).replaceAll(any());
  }

  @Test
  void rejectsAnOpenDayWithoutTimes() {
    List<OpeningHoursDay> week = new ArrayList<>(week());
    week.set(1, OpeningHoursDay.builder().dayOfWeek(DayOfWeek.TUESDAY).closed(false).build());

    assertThrows(InvalidConfigurationException.class, () -> service.update(week));
    verify(port, never()).replaceAll(any());
  }

  @Test
  void rejectsClosingBeforeOpening() {
    List<OpeningHoursDay> week = new ArrayList<>(week());
    week.set(
        1,
        OpeningHoursDay.builder()
            .dayOfWeek(DayOfWeek.TUESDAY)
            .closed(false)
            .openTime(LocalTime.of(22, 0))
            .closeTime(LocalTime.of(8, 0))
            .build());

    assertThrows(InvalidConfigurationException.class, () -> service.update(week));
    verify(port, never()).replaceAll(any());
  }

  @Test
  void savesValidWeekAndReturnsStoredOrder() {
    when(port.findAll()).thenReturn(week());

    List<OpeningHoursDay> result = service.update(week());

    verify(port).replaceAll(week());
    assertEquals(7, result.size());
    assertEquals(LocalTime.of(8, 0), result.get(1).getOpenTime());
    assertEquals(LocalTime.of(22, 0), result.get(1).getCloseTime());
  }
}
