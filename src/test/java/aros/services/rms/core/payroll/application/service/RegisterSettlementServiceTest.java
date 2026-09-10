/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.exception.PayrollSettlementException;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link RegisterSettlementService}. */
@ExtendWith(MockitoExtension.class)
class RegisterSettlementServiceTest {

  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private Logger logger;

  private RegisterSettlementService service;

  @BeforeEach
  void setUp() {
    service = new RegisterSettlementService(settlementRepository, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_save_settlement
  // ---------------------------------------------------------------------------

  @Test
  void should_save_settlement() {
    PayrollSettlement settlement =
        PayrollSettlement.create(
            10L,
            1L,
            SettlementType.MONTHLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("2500000"),
            "August salary",
            "admin");

    PayrollSettlement saved =
        new PayrollSettlement(
            1L,
            10L,
            1L,
            SettlementType.MONTHLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("2500000"),
            "August salary",
            Instant.now(),
            "admin");

    when(settlementRepository.save(any())).thenReturn(saved);

    PayrollSettlement result = service.execute(settlement);

    ArgumentCaptor<PayrollSettlement> captor = ArgumentCaptor.forClass(PayrollSettlement.class);
    verify(settlementRepository).save(captor.capture());
    assertNotNull(captor.getValue());
    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals(10L, result.payrollId());
    assertEquals(SettlementType.MONTHLY, result.settlementType());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_reject_negative_amount
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_negative_amount() {
    PayrollSettlement settlement =
        new PayrollSettlement(
            null,
            10L,
            1L,
            SettlementType.MONTHLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("-100"),
            null,
            null,
            "admin");

    PayrollSettlementException ex =
        assertThrows(PayrollSettlementException.class, () -> service.execute(settlement));

    assertNotNull(ex);
    assertEquals("Settlement amount must be positive", ex.getMessage());
    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_zero_amount
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_zero_amount() {
    PayrollSettlement settlement =
        new PayrollSettlement(
            null,
            10L,
            1L,
            SettlementType.WEEKLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 7),
            BigDecimal.ZERO,
            null,
            null,
            "admin");

    PayrollSettlementException ex =
        assertThrows(PayrollSettlementException.class, () -> service.execute(settlement));

    assertNotNull(ex);
    assertEquals("Settlement amount must be positive", ex.getMessage());
    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_reject_null_amount
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_null_amount() {
    PayrollSettlement settlement =
        new PayrollSettlement(
            null,
            10L,
            1L,
            SettlementType.DAILY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 1),
            null,
            null,
            null,
            "admin");

    PayrollSettlementException ex =
        assertThrows(PayrollSettlementException.class, () -> service.execute(settlement));

    assertNotNull(ex);
    assertEquals("Settlement amount must be positive", ex.getMessage());
    verify(settlementRepository, never()).save(any());
  }
}
