/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.exception.PayrollEventNotFoundException;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link DeletePayrollEventService}. */
@ExtendWith(MockitoExtension.class)
class DeletePayrollEventServiceTest {

  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private Logger logger;

  private DeletePayrollEventService service;

  @BeforeEach
  void setUp() {
    service = new DeletePayrollEventService(eventRepository, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_delete_event
  // ---------------------------------------------------------------------------

  @Test
  void should_delete_event() {
    PayrollEvent existing =
        new PayrollEvent(
            1L,
            1L,
            LocalDate.of(2026, 8, 1),
            PayrollEventType.OVERTIME,
            new BigDecimal("2.5"),
            new BigDecimal("23437.50"),
            new BigDecimal("58593.75"),
            null,
            Instant.now(),
            "admin");

    when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));

    assertDoesNotThrow(() -> service.execute(1L));
    verify(eventRepository).deleteById(1L);
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_throw_exception_when_event_not_found
  // ---------------------------------------------------------------------------

  @Test
  void should_throw_exception_when_event_not_found() {
    when(eventRepository.findById(99L)).thenReturn(Optional.empty());

    PayrollEventNotFoundException ex =
        assertThrows(PayrollEventNotFoundException.class, () -> service.execute(99L));

    assert ex.getMessage().contains("99");
    verify(eventRepository, never()).deleteById(99L);
  }
}
