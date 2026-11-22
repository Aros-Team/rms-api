/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.exception.PayrollAggregationException;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link PayrollAggregationService}.
 *
 * <p><b>Feature:</b> Rebuild the payroll bonuses, deductions, net and paid amounts from the
 * persisted events and settlements.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Earnings add to bonuses and deduction types add to deductions
 *   <li>The totals are recomputed, so two runs with the same rows do not double count
 *   <li>Settlements rebuild the paid amount
 *   <li>An event outside the payroll period or for another worker is ignored
 *   <li>An aggregation that would push paid above net is rejected
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the repository ports and asserts the saved payroll exactly
 *   <li>Feeds hand-built events and settlements to the real aggregation service
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PayrollAggregationServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;

  @Mock private PayrollRepositoryPort payrollRepository;
  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private Logger logger;

  private PayrollAggregationService service;

  @BeforeEach
  void setUp() {
    service =
        new PayrollAggregationService(
            payrollRepository, eventRepository, settlementRepository, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_add_earnings_to_bonuses_and_deductions_to_deductions
  // ---------------------------------------------------------------------------

  @Test
  void should_add_earnings_to_bonuses_and_deductions_to_deductions() {
    Payroll payroll = payroll(money("1500000"), money("100000"), money("20000"));
    when(eventRepository.findByUserAndPeriod(USER_ID, payroll.periodStart(), payroll.periodEnd()))
        .thenReturn(
            List.of(
                event(1L, USER_ID, LocalDate.of(2026, 8, 5), PayrollEventType.OVERTIME, "40000"),
                event(2L, USER_ID, LocalDate.of(2026, 8, 6), PayrollEventType.DEDUCTION, "30000")));
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Payroll result = service.recompute(payroll);

    assertEquals(0, money("140000").compareTo(result.bonuses()));
    assertEquals(0, money("50000").compareTo(result.deductions()));
    assertEquals(0, money("1590000").compareTo(result.netAmount()));
    assertEquals(0, Money.zero(COP).compareTo(result.paidAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_not_double_count_on_repeated_recompute
  // ---------------------------------------------------------------------------

  @Test
  void should_not_double_count_on_repeated_recompute() {
    Payroll payroll = payroll(money("1500000"), money("0"), money("0"));
    List<PayrollEvent> events =
        List.of(
            event(
                1L,
                USER_ID,
                LocalDate.of(2026, 8, 5),
                PayrollEventType.BONUS_PERFORMANCE,
                "50000"));
    when(eventRepository.findByUserAndPeriod(USER_ID, payroll.periodStart(), payroll.periodEnd()))
        .thenReturn(events);
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Payroll first = service.recompute(payroll);
    Payroll second = service.recompute(first);

    assertEquals(0, money("50000").compareTo(first.bonuses()));
    assertEquals(0, money("50000").compareTo(second.bonuses()));
    assertEquals(0, first.netAmount().compareTo(second.netAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_rebuild_paid_amount_from_settlements
  // ---------------------------------------------------------------------------

  @Test
  void should_rebuild_paid_amount_from_settlements() {
    Payroll payroll = payroll(money("1500000"), money("0"), money("0"));
    when(eventRepository.findByUserAndPeriod(USER_ID, payroll.periodStart(), payroll.periodEnd()))
        .thenReturn(List.of());
    when(settlementRepository.findByPayrollId(1L))
        .thenReturn(List.of(settlement(1L, "400000", 1, 15), settlement(2L, "300000", 16, 31)));
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Payroll result = service.recompute(payroll);

    assertEquals(0, money("700000").compareTo(result.paidAmount()));
    assertEquals(0, money("800000").compareTo(result.pendingAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_ignore_events_outside_period_or_worker
  // ---------------------------------------------------------------------------

  @Test
  void should_ignore_events_outside_period_or_worker() {
    Payroll payroll = payroll(money("1500000"), money("0"), money("0"));
    when(eventRepository.findByUserAndPeriod(USER_ID, payroll.periodStart(), payroll.periodEnd()))
        .thenReturn(
            List.of(
                event(
                    1L,
                    999L,
                    LocalDate.of(2026, 8, 5),
                    PayrollEventType.BONUS_PERFORMANCE,
                    "50000"),
                event(
                    2L,
                    USER_ID,
                    LocalDate.of(2026, 9, 5),
                    PayrollEventType.BONUS_PERFORMANCE,
                    "70000")));
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Payroll result = service.recompute(payroll);

    assertEquals(0, Money.zero(COP).compareTo(result.bonuses()));
    assertEquals(0, money("1500000").compareTo(result.netAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_reject_paid_above_net
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_paid_above_net() {
    Payroll payroll = payroll(money("1000000"), money("0"), money("0"));
    when(eventRepository.findByUserAndPeriod(USER_ID, payroll.periodStart(), payroll.periodEnd()))
        .thenReturn(List.of());
    when(settlementRepository.findByPayrollId(1L))
        .thenReturn(List.of(settlement(1L, "1500000", 1, 31)));

    assertThrows(PayrollAggregationException.class, () -> service.recompute(payroll));
    verify(payrollRepository, org.mockito.Mockito.never()).save(any());
  }

  private PayrollEvent event(
      Long id, Long userId, LocalDate date, PayrollEventType type, String amount) {
    Money value = money(amount);
    return new PayrollEvent(
        id, userId, date, type, BigDecimal.ONE, value, value, null, Instant.now(), "admin");
  }

  private PayrollSettlement settlement(Long id, String amount, int startDay, int endDay) {
    return new PayrollSettlement(
        id,
        1L,
        USER_ID,
        SettlementType.BIWEEKLY,
        LocalDate.of(2026, 8, startDay),
        LocalDate.of(2026, 8, endDay),
        money(amount),
        null,
        Instant.now(),
        "admin");
  }

  private Payroll payroll(Money baseSalary, Money manualBonuses, Money manualDeductions) {
    Money net = baseSalary.plus(manualBonuses).minus(manualDeductions);
    return new Payroll(
        1L,
        USER_ID,
        YearMonth.of(2026, 8),
        LocalDate.of(2026, 8, 1),
        LocalDate.of(2026, 8, 31),
        baseSalary,
        manualBonuses,
        manualDeductions,
        manualBonuses,
        manualDeductions,
        net,
        Money.zero(COP),
        new BigDecimal("192"),
        PayrollStatus.PENDING,
        null,
        10L,
        Instant.now(),
        Instant.now());
  }

  private Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
