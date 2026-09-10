/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link ListPayrollEventsService}. */
@ExtendWith(MockitoExtension.class)
class ListPayrollEventsServiceTest {

  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private Logger logger;

  private ListPayrollEventsService service;

  @BeforeEach
  void setUp() {
    service = new ListPayrollEventsService(eventRepository, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_list_events_for_period
  // ---------------------------------------------------------------------------

  @Test
  void should_list_events_for_period() {
    LocalDate startDate = LocalDate.of(2026, 8, 1);
    LocalDate endDate = LocalDate.of(2026, 8, 31);

    PayrollEvent event1 =
        new PayrollEvent(
            1L,
            1L,
            LocalDate.of(2026, 8, 5),
            PayrollEventType.OVERTIME,
            new BigDecimal("2.5"),
            new BigDecimal("23437.50"),
            new BigDecimal("58593.75"),
            null,
            Instant.now(),
            "admin");

    PayrollEvent event2 =
        new PayrollEvent(
            2L,
            1L,
            LocalDate.of(2026, 8, 20),
            PayrollEventType.BONUS_ATTENDANCE,
            new BigDecimal("1"),
            new BigDecimal("7812.50"),
            new BigDecimal("7812.50"),
            "Attendance bonus",
            Instant.now(),
            "admin");

    when(eventRepository.findByUserAndPeriod(1L, startDate, endDate))
        .thenReturn(List.of(event1, event2));

    List<PayrollEvent> result = service.execute(1L, startDate, endDate);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).id());
    assertEquals(2L, result.get(1).id());
    assertEquals(PayrollEventType.OVERTIME, result.get(0).eventType());
    assertEquals(PayrollEventType.BONUS_ATTENDANCE, result.get(1).eventType());
    verify(eventRepository).findByUserAndPeriod(1L, startDate, endDate);
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_return_empty_list_when_no_events
  // ---------------------------------------------------------------------------

  @Test
  void should_return_empty_list_when_no_events() {
    LocalDate startDate = LocalDate.of(2026, 8, 1);
    LocalDate endDate = LocalDate.of(2026, 8, 31);

    when(eventRepository.findByUserAndPeriod(99L, startDate, endDate)).thenReturn(List.of());

    List<PayrollEvent> result = service.execute(99L, startDate, endDate);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(eventRepository).findByUserAndPeriod(99L, startDate, endDate);
  }
}
