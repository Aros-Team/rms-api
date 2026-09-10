/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link RegisterPayrollEventService}. */
@ExtendWith(MockitoExtension.class)
class RegisterPayrollEventServiceTest {

  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private Logger logger;

  private RegisterPayrollEventService service;

  @BeforeEach
  void setUp() {
    service = new RegisterPayrollEventService(eventRepository, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_save_event
  // ---------------------------------------------------------------------------

  @Test
  void should_save_event() {
    PayrollEvent event =
        PayrollEvent.create(
            1L,
            LocalDate.of(2026, 8, 1),
            PayrollEventType.OVERTIME,
            new BigDecimal("2.5"),
            new BigDecimal("23437.50"),
            "Weekend extra",
            "admin");

    PayrollEvent saved =
        new PayrollEvent(
            1L,
            1L,
            LocalDate.of(2026, 8, 1),
            PayrollEventType.OVERTIME,
            new BigDecimal("2.5"),
            new BigDecimal("23437.50"),
            new BigDecimal("58593.75"),
            "Weekend extra",
            Instant.now(),
            "admin");

    when(eventRepository.save(any())).thenReturn(saved);

    PayrollEvent result = service.execute(event);

    ArgumentCaptor<PayrollEvent> captor = ArgumentCaptor.forClass(PayrollEvent.class);
    verify(eventRepository).save(captor.capture());
    assertNotNull(captor.getValue());
    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals(1L, result.userId());
    assertEquals(PayrollEventType.OVERTIME, result.eventType());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_return_saved_event
  // ---------------------------------------------------------------------------

  @Test
  void should_return_saved_event() {
    PayrollEvent event =
        PayrollEvent.create(
            2L,
            LocalDate.of(2026, 8, 15),
            PayrollEventType.DEDUCTION,
            new BigDecimal("1"),
            new BigDecimal("50000"),
            "Missing uniform",
            "system");

    PayrollEvent saved =
        new PayrollEvent(
            5L,
            2L,
            LocalDate.of(2026, 8, 15),
            PayrollEventType.DEDUCTION,
            new BigDecimal("1"),
            new BigDecimal("50000"),
            new BigDecimal("-50000"),
            "Missing uniform",
            Instant.now(),
            "system");

    when(eventRepository.save(any())).thenReturn(saved);

    PayrollEvent result = service.execute(event);

    assertEquals(5L, result.id());
    assertEquals(2L, result.userId());
    assertEquals(0, new BigDecimal("50000").compareTo(result.unitRate()));
  }
}
