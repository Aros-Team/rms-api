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
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.exception.PayrollSettlementException;
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
 * Tests for {@link RegisterSettlementService}.
 *
 * <p><b>Feature:</b> Register a payment against a payroll and keep paid/pending consistent.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>A valid settlement is saved and the payroll paid amount is recomputed
 *   <li>An amount above the pending amount is rejected
 *   <li>A worker or period mismatch with the payroll is rejected
 *   <li>A settlement overlapping an existing one is rejected (repeated payment)
 *   <li>A PAID payroll is immutable
 *   <li>A missing payroll is a 404-domain error
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the ports and runs the real {@link PayrollAggregationService}
 *   <li>An in-memory settlement list backs the overlap and paid recomputation checks
 *   <li>The saved payroll is captured to assert paid and pending amounts
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RegisterSettlementServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;
  private static final Long PAYROLL_ID = 10L;

  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private PayrollRepositoryPort payrollRepository;
  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private Logger logger;

  private final List<PayrollSettlement> storedSettlements = new ArrayList<>();
  private RegisterSettlementService service;

  @BeforeEach
  void setUp() {
    storedSettlements.clear();
    PayrollAggregationService aggregation =
        new PayrollAggregationService(
            payrollRepository, eventRepository, settlementRepository, logger);
    service =
        new RegisterSettlementService(settlementRepository, payrollRepository, aggregation, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_save_settlement_and_update_paid_amount
  // ---------------------------------------------------------------------------

  @Test
  void should_save_settlement_and_update_paid_amount() {
    Payroll payroll = payroll(PayrollStatus.PENDING, money("2000000"), money("0"));
    when(payrollRepository.findById(PAYROLL_ID)).thenReturn(Optional.of(payroll));
    when(settlementRepository.findByPayrollId(PAYROLL_ID)).thenAnswer(i -> storedSettlements);
    when(settlementRepository.save(any()))
        .thenAnswer(invocation -> store(invocation.getArgument(0)));
    when(eventRepository.findByUserAndPeriod(any(), any(), any())).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    PayrollSettlement result = service.execute(settlement(USER_ID, "800000", 1, 15));

    assertEquals(1L, result.id());

    ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
    verify(payrollRepository).save(captor.capture());
    assertEquals(0, money("800000").compareTo(captor.getValue().paidAmount()));
    assertEquals(0, money("1200000").compareTo(captor.getValue().pendingAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_reject_overpayment
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_overpayment() {
    when(payrollRepository.findById(PAYROLL_ID))
        .thenReturn(Optional.of(payroll(PayrollStatus.PENDING, money("1000000"), money("0"))));
    when(settlementRepository.findByPayrollId(PAYROLL_ID)).thenReturn(List.of());

    PayrollSettlementException exception =
        assertThrows(
            PayrollSettlementException.class,
            () -> service.execute(settlement(USER_ID, "1000001", 1, 31)));

    assertEquals(true, exception.getMessage().contains("supera el monto pendiente"));
    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_worker_mismatch
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_worker_mismatch() {
    when(payrollRepository.findById(PAYROLL_ID))
        .thenReturn(Optional.of(payroll(PayrollStatus.PENDING, money("1000000"), money("0"))));

    PayrollSettlementException exception =
        assertThrows(
            PayrollSettlementException.class,
            () -> service.execute(settlement(999L, "100000", 1, 31)));

    assertEquals(true, exception.getMessage().contains("no coincide con el usuario de la nómina"));
    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_reject_period_outside_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_period_outside_payroll() {
    when(payrollRepository.findById(PAYROLL_ID))
        .thenReturn(Optional.of(payroll(PayrollStatus.PENDING, money("1000000"), money("0"))));

    PayrollSettlementException exception =
        assertThrows(
            PayrollSettlementException.class,
            () ->
                service.execute(
                    PayrollSettlement.create(
                        PAYROLL_ID,
                        USER_ID,
                        SettlementType.BIWEEKLY,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 15),
                        money("100000"),
                        null,
                        "admin")));

    assertEquals(true, exception.getMessage().contains("fuera del periodo de nómina"));
    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_reject_overlapping_duplicate
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_overlapping_duplicate() {
    when(payrollRepository.findById(PAYROLL_ID))
        .thenReturn(Optional.of(payroll(PayrollStatus.PENDING, money("2000000"), money("0"))));
    storedSettlements.add(persistedSettlement(1L, money("500000"), 1, 15));
    when(settlementRepository.findByPayrollId(PAYROLL_ID)).thenAnswer(i -> storedSettlements);

    PayrollSettlementException exception =
        assertThrows(
            PayrollSettlementException.class,
            () -> service.execute(settlement(USER_ID, "500000", 1, 15)));

    assertEquals(true, exception.getMessage().contains("se superpone con la liquidación 1"));
    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-06: should_reject_paid_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_paid_payroll() {
    when(payrollRepository.findById(PAYROLL_ID))
        .thenReturn(Optional.of(payroll(PayrollStatus.PAID, money("1000000"), money("0"))));

    assertThrows(
        PayrollImmutableException.class,
        () -> service.execute(settlement(USER_ID, "100000", 1, 31)));

    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-07: should_reject_missing_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_missing_payroll() {
    when(payrollRepository.findById(PAYROLL_ID)).thenReturn(Optional.empty());

    assertThrows(
        PayrollNotFoundException.class,
        () -> service.execute(settlement(USER_ID, "100000", 1, 31)));

    verify(settlementRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-08: should_reject_non_positive_amount
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_non_positive_amount() {
    PayrollSettlement settlement =
        PayrollSettlement.create(
            PAYROLL_ID,
            USER_ID,
            SettlementType.WEEKLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 7),
            Money.zero(COP),
            null,
            "admin");

    PayrollSettlementException exception =
        assertThrows(PayrollSettlementException.class, () -> service.execute(settlement));

    assertEquals("El monto de la liquidación debe ser positivo", exception.getMessage());
    verify(settlementRepository, never()).save(any());
  }

  private PayrollSettlement store(PayrollSettlement settlement) {
    PayrollSettlement persisted =
        new PayrollSettlement(
            (long) storedSettlements.size() + 1,
            settlement.payrollId(),
            settlement.userId(),
            settlement.settlementType(),
            settlement.periodStart(),
            settlement.periodEnd(),
            settlement.amount(),
            settlement.notes(),
            Instant.now(),
            settlement.settledBy());
    storedSettlements.add(persisted);
    return persisted;
  }

  private PayrollSettlement persistedSettlement(Long id, Money amount, int startDay, int endDay) {
    return new PayrollSettlement(
        id,
        PAYROLL_ID,
        USER_ID,
        SettlementType.BIWEEKLY,
        LocalDate.of(2026, 8, startDay),
        LocalDate.of(2026, 8, endDay),
        amount,
        null,
        Instant.now(),
        "admin");
  }

  private PayrollSettlement settlement(Long userId, String amount, int startDay, int endDay) {
    return PayrollSettlement.create(
        PAYROLL_ID,
        userId,
        SettlementType.BIWEEKLY,
        LocalDate.of(2026, 8, startDay),
        LocalDate.of(2026, 8, endDay),
        money(amount),
        null,
        "admin");
  }

  private Payroll payroll(PayrollStatus status, Money baseSalary, Money paidAmount) {
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
        paidAmount,
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
