/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
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
 * Tests for {@link UpdatePayrollService}.
 *
 * <p><b>Feature:</b> Partial payroll update keeps the manual component separate and re-aggregates
 * the persisted events.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>A manual bonus update is added on top of the persisted events
 *   <li>A status transition follows the PENDING → ACCRUED → PAID rules
 *   <li>A PAID payroll is immutable
 *   <li>A missing payroll is a 404-domain error
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the ports and runs the real {@link PayrollAggregationService}
 *   <li>Captures the saved payroll to assert the final totals
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UpdatePayrollServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;

  @Mock private PayrollRepositoryPort payrollRepository;
  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private Logger logger;

  private UpdatePayrollService service;

  @BeforeEach
  void setUp() {
    PayrollAggregationService aggregation =
        new PayrollAggregationService(
            payrollRepository, eventRepository, settlementRepository, logger);
    service = new UpdatePayrollService(payrollRepository, aggregation, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_apply_manual_bonus_on_top_of_events
  // ---------------------------------------------------------------------------

  @Test
  void should_apply_manual_bonus_on_top_of_events() {
    Payroll payroll = payroll(PayrollStatus.PENDING);
    when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));
    when(eventRepository.findByUserAndPeriod(USER_ID, payroll.periodStart(), payroll.periodEnd()))
        .thenReturn(
            List.of(
                event(1L, PayrollEventType.BONUS_PERFORMANCE, LocalDate.of(2026, 8, 5), "50000")));
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.update(
        1L,
        new UpdatePayrollUseCase.UpdatePayrollCommand(
            null, money("100000"), null, null, PayrollStatus.ACCRUED, null, 10L));

    ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
    org.mockito.Mockito.verify(payrollRepository).save(captor.capture());
    Payroll saved = captor.getValue();
    assertEquals(0, money("100000").compareTo(saved.manualBonuses()));
    assertEquals(0, money("150000").compareTo(saved.bonuses()));
    assertEquals(0, money("1650000").compareTo(saved.netAmount()));
    assertEquals(PayrollStatus.ACCRUED, saved.status());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_reject_invalid_transition
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_invalid_transition() {
    when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll(PayrollStatus.ACCRUED)));

    assertThrows(
        PayrollImmutableException.class,
        () ->
            service.update(
                1L,
                new UpdatePayrollUseCase.UpdatePayrollCommand(
                    null, null, null, null, PayrollStatus.PENDING, null, 10L)));
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_paid_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_paid_payroll() {
    when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll(PayrollStatus.PAID)));

    assertThrows(
        PayrollImmutableException.class,
        () ->
            service.update(
                1L,
                new UpdatePayrollUseCase.UpdatePayrollCommand(
                    null, money("1000"), null, null, null, null, 10L)));
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_reject_missing_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_missing_payroll() {
    when(payrollRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        PayrollNotFoundException.class,
        () ->
            service.update(
                99L,
                new UpdatePayrollUseCase.UpdatePayrollCommand(
                    null, null, null, null, null, null, 10L)));
  }

  private Payroll payroll(PayrollStatus status) {
    return new Payroll(
        1L,
        USER_ID,
        YearMonth.of(2026, 8),
        LocalDate.of(2026, 8, 1),
        LocalDate.of(2026, 8, 31),
        money("1500000"),
        Money.zero(COP),
        Money.zero(COP),
        Money.zero(COP),
        Money.zero(COP),
        money("1500000"),
        Money.zero(COP),
        new BigDecimal("192"),
        status,
        null,
        10L,
        Instant.parse("2026-08-01T00:00:00Z"),
        Instant.parse("2026-08-01T00:00:00Z"));
  }

  private PayrollEvent event(Long id, PayrollEventType type, LocalDate date, String amount) {
    Money value = money(amount);
    return new PayrollEvent(
        id, USER_ID, date, type, BigDecimal.ONE, value, value, null, Instant.now(), "admin");
  }

  private Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
