/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollSettlementNotFoundException;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link VoidSettlementService}.
 *
 * <p><b>Feature:</b> Void a settlement and restore the payroll pending amount exactly.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Voiding removes the payment and recomputes paid/pending from the remaining payments
 *   <li>A missing settlement is a 404-domain error
 *   <li>A PAID payroll is immutable and cannot be voided
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the ports and runs the real {@link PayrollAggregationService}
 *   <li>The in-memory settlement list is updated by the mocked delete call
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class VoidSettlementServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;
  private static final Long PAYROLL_ID = 10L;

  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private PayrollRepositoryPort payrollRepository;
  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private Logger logger;

  private final List<PayrollSettlement> storedSettlements = new ArrayList<>();
  private VoidSettlementService service;

  @BeforeEach
  void setUp() {
    storedSettlements.clear();
    PayrollAggregationService aggregation =
        new PayrollAggregationService(
            payrollRepository, eventRepository, settlementRepository, logger);
    service =
        new VoidSettlementService(settlementRepository, payrollRepository, aggregation, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_void_settlement_and_restore_pending
  // ---------------------------------------------------------------------------

  @Test
  void should_void_settlement_and_restore_pending() {
    PayrollSettlement first = settlement(1L, "400000", 1, 15);
    PayrollSettlement second = settlement(2L, "300000", 16, 31);
    storedSettlements.add(first);
    storedSettlements.add(second);

    Payroll payroll = payroll(PayrollStatus.PENDING, money("2000000"));
    when(settlementRepository.findById(1L)).thenReturn(Optional.of(first));
    when(payrollRepository.findById(PAYROLL_ID)).thenReturn(Optional.of(payroll));
    org.mockito.Mockito.doAnswer(
            invocation -> {
              storedSettlements.removeIf(s -> s.id().equals(1L));
              return null;
            })
        .when(settlementRepository)
        .deleteById(1L);
    when(settlementRepository.findByPayrollId(PAYROLL_ID)).thenAnswer(i -> storedSettlements);
    when(eventRepository.findByUserAndPeriod(any(), any(), any())).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.execute(1L);

    ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
    verify(payrollRepository).save(captor.capture());
    assertEquals(0, money("300000").compareTo(captor.getValue().paidAmount()));
    assertEquals(0, money("1700000").compareTo(captor.getValue().pendingAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_reject_missing_settlement
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_missing_settlement() {
    when(settlementRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(PayrollSettlementNotFoundException.class, () -> service.execute(99L));

    verify(settlementRepository, never()).deleteById(99L);
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_void_on_paid_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_void_on_paid_payroll() {
    PayrollSettlement first = settlement(1L, "400000", 1, 15);
    when(settlementRepository.findById(1L)).thenReturn(Optional.of(first));
    when(payrollRepository.findById(PAYROLL_ID))
        .thenReturn(Optional.of(payroll(PayrollStatus.PAID, money("2000000"))));

    assertThrows(PayrollImmutableException.class, () -> service.execute(1L));

    verify(settlementRepository, never()).deleteById(1L);
  }

  private PayrollSettlement settlement(Long id, String amount, int startDay, int endDay) {
    return new PayrollSettlement(
        id,
        PAYROLL_ID,
        USER_ID,
        SettlementType.BIWEEKLY,
        LocalDate.of(2026, 8, startDay),
        LocalDate.of(2026, 8, endDay),
        money(amount),
        null,
        Instant.now(),
        "admin");
  }

  private Payroll payroll(PayrollStatus status, Money baseSalary) {
    return new Payroll(
        PAYROLL_ID,
        USER_ID,
        YearMonth.of(2026, 8),
        LocalDate.of(2026, 8, 1),
        LocalDate.of(2026, 8, 31),
        baseSalary,
        Money.zero(COP),
        Money.zero(COP),
        Money.zero(COP),
        Money.zero(COP),
        baseSalary,
        Money.zero(COP),
        new BigDecimal("192"),
        status,
        null,
        10L,
        Instant.now(),
        Instant.now());
  }

  private Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
