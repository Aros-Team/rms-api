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
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.exception.PayrollEventNotFoundException;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
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
 * Tests for {@link DeletePayrollEventService}.
 *
 * <p><b>Feature:</b> Delete a payroll event and reverse exactly its effect on the payroll.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Deleting an event removes its contribution and re-aggregates the payroll
 *   <li>A missing event is a 404-domain error and nothing is deleted
 *   <li>An event on a PAID payroll cannot be deleted
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the ports and runs the real {@link PayrollAggregationService}
 *   <li>The in-memory event list is updated by the mocked delete call
 *   <li>The saved payroll is captured to assert the exact reversed totals
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class DeletePayrollEventServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;

  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private PayrollRepositoryPort payrollRepository;
  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private Logger logger;

  private final List<PayrollEvent> storedEvents = new ArrayList<>();
  private DeletePayrollEventService service;

  @BeforeEach
  void setUp() {
    storedEvents.clear();
    PayrollAggregationService aggregation =
        new PayrollAggregationService(
            payrollRepository, eventRepository, settlementRepository, logger);
    service =
        new DeletePayrollEventService(eventRepository, payrollRepository, aggregation, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_delete_event_and_reverse_its_effect
  // ---------------------------------------------------------------------------

  @Test
  void should_delete_event_and_reverse_its_effect() {
    PayrollEvent bonus = event(1L, PayrollEventType.BONUS_PERFORMANCE, "50000");
    PayrollEvent secondBonus = event(2L, PayrollEventType.BONUS_PERFORMANCE, "30000");
    storedEvents.add(bonus);
    storedEvents.add(secondBonus);

    Payroll payroll = payroll(PayrollStatus.PENDING, money("1500000"));
    when(eventRepository.findById(1L)).thenReturn(Optional.of(bonus));
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8))
        .thenReturn(Optional.of(payroll));
    org.mockito.Mockito.doAnswer(
            invocation -> {
              storedEvents.removeIf(e -> e.id().equals(1L));
              return null;
            })
        .when(eventRepository)
        .deleteById(1L);
    when(eventRepository.findByUserAndPeriod(any(), any(), any())).thenAnswer(i -> storedEvents);
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.execute(1L);

    ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
    verify(payrollRepository).save(captor.capture());
    assertEquals(0, money("30000").compareTo(captor.getValue().bonuses()));
    assertEquals(0, money("1530000").compareTo(captor.getValue().netAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_throw_when_event_not_found
  // ---------------------------------------------------------------------------

  @Test
  void should_throw_when_event_not_found() {
    when(eventRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(PayrollEventNotFoundException.class, () -> service.execute(99L));

    verify(eventRepository, never()).deleteById(99L);
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_delete_on_paid_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_delete_on_paid_payroll() {
    PayrollEvent bonus = event(1L, PayrollEventType.BONUS_PERFORMANCE, "50000");
    when(eventRepository.findById(1L)).thenReturn(Optional.of(bonus));
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8))
        .thenReturn(Optional.of(payroll(PayrollStatus.PAID, money("1500000"))));

    assertThrows(PayrollImmutableException.class, () -> service.execute(1L));

    verify(eventRepository, never()).deleteById(1L);
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_reject_delete_that_would_overpay
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_delete_that_would_overpay() {
    PayrollEvent bonus = event(1L, PayrollEventType.BONUS_PERFORMANCE, "100000");
    storedEvents.add(bonus);
    when(eventRepository.findById(1L)).thenReturn(Optional.of(bonus));
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8))
        .thenReturn(Optional.of(payrollWithPaid(money("1000000"), money("1050000"))));
    when(eventRepository.findByUserAndPeriod(any(), any(), any())).thenAnswer(i -> storedEvents);
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of(settlement("1050000")));

    assertThrows(
        aros.services.rms.core.payroll.domain.exception.PayrollAggregationException.class,
        () -> service.execute(1L));

    verify(eventRepository, never()).deleteById(1L);
  }

  private PayrollSettlement settlement(String amount) {
    return new PayrollSettlement(
        9L,
        1L,
        USER_ID,
        SettlementType.MONTHLY,
        LocalDate.of(2026, 8, 1),
        LocalDate.of(2026, 8, 31),
        money(amount),
        null,
        Instant.now(),
        "admin");
  }

  private PayrollEvent event(Long id, PayrollEventType type, String amount) {
    Money value = money(amount);
    return new PayrollEvent(
        id,
        USER_ID,
        LocalDate.of(2026, 8, 5),
        type,
        BigDecimal.ONE,
        value,
        value,
        null,
        Instant.now(),
        "admin");
  }

  private Payroll payrollWithPaid(Money baseSalary, Money paidAmount) {
    return new Payroll(
        1L,
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
        PayrollStatus.PENDING,
        null,
        10L,
        Instant.now(),
        Instant.now());
  }

  private Payroll payroll(PayrollStatus status, Money baseSalary) {
    return new Payroll(
        1L,
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
        status == PayrollStatus.PAID ? baseSalary : Money.zero(COP),
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
