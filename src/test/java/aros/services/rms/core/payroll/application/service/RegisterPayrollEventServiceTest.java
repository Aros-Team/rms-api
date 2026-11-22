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
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.exception.InvalidPayrollPeriodException;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
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
 * Tests for {@link RegisterPayrollEventService}.
 *
 * <p><b>Feature:</b> Register a payroll event and fold it into the payroll totals.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>A valid event is persisted and the payroll bonuses/net are recomputed
 *   <li>An event without a payroll is rejected
 *   <li>An event outside the payroll period is rejected
 *   <li>An event on a PAID payroll is rejected
 *   <li>Registering two events accumulates each one exactly once
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the repository ports and runs the real {@link PayrollAggregationService}
 *   <li>The event repository keeps an in-memory list so the aggregation sees persisted events
 *   <li>The payroll repository captures the saved payroll for assertions
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RegisterPayrollEventServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;
  private static final LocalDate EVENT_DATE = LocalDate.of(2026, 8, 5);

  @Mock private PayrollEventRepositoryPort eventRepository;
  @Mock private PayrollRepositoryPort payrollRepository;
  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private Logger logger;

  private final List<PayrollEvent> storedEvents = new ArrayList<>();
  private RegisterPayrollEventService service;

  @BeforeEach
  void setUp() {
    storedEvents.clear();
    PayrollAggregationService aggregation =
        new PayrollAggregationService(
            payrollRepository, eventRepository, settlementRepository, logger);
    service =
        new RegisterPayrollEventService(eventRepository, payrollRepository, aggregation, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_save_event_and_recompute_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_save_event_and_recompute_payroll() {
    Payroll payroll = payroll(PayrollStatus.PENDING, money("1500000"), money("100000"), money("0"));
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8))
        .thenReturn(Optional.of(payroll));
    when(eventRepository.save(any())).thenAnswer(invocation -> store(invocation.getArgument(0)));
    when(eventRepository.findByUserAndPeriod(any(), any(), any())).thenAnswer(i -> storedEvents);
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    PayrollEvent result =
        service.execute(
            PayrollEvent.create(
                USER_ID,
                EVENT_DATE,
                PayrollEventType.OVERTIME,
                new BigDecimal("2"),
                money("10000"),
                "Overtime",
                "admin"));

    assertEquals(1L, result.id());

    ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
    verify(payrollRepository).save(captor.capture());
    Payroll saved = captor.getValue();
    assertEquals(0, money("120000").compareTo(saved.bonuses()));
    assertEquals(0, money("1620000").compareTo(saved.netAmount()));
    assertEquals(1, storedEvents.size());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_reject_event_without_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_event_without_payroll() {
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8)).thenReturn(Optional.empty());

    assertThrows(
        PayrollNotFoundException.class,
        () ->
            service.execute(
                PayrollEvent.create(
                    USER_ID,
                    EVENT_DATE,
                    PayrollEventType.BONUS_PERFORMANCE,
                    BigDecimal.ONE,
                    money("50000"),
                    null,
                    "admin")));

    verify(eventRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_event_outside_payroll_period
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_event_outside_payroll_period() {
    Payroll payroll = payroll(PayrollStatus.PENDING, money("1500000"), money("0"), money("0"));
    when(payrollRepository.findByUserIdAndPeriod(
            org.mockito.ArgumentMatchers.eq(USER_ID),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.anyInt()))
        .thenReturn(Optional.of(payroll));

    assertThrows(
        InvalidPayrollPeriodException.class,
        () ->
            service.execute(
                PayrollEvent.create(
                    USER_ID,
                    LocalDate.of(2026, 9, 1),
                    PayrollEventType.OVERTIME,
                    BigDecimal.ONE,
                    money("10000"),
                    null,
                    "admin")));

    verify(eventRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_reject_event_on_paid_payroll
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_event_on_paid_payroll() {
    Payroll payroll = payroll(PayrollStatus.PAID, money("1500000"), money("0"), money("0"));
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8))
        .thenReturn(Optional.of(payroll));

    assertThrows(
        PayrollImmutableException.class,
        () ->
            service.execute(
                PayrollEvent.create(
                    USER_ID,
                    EVENT_DATE,
                    PayrollEventType.OVERTIME,
                    BigDecimal.ONE,
                    money("10000"),
                    null,
                    "admin")));

    verify(eventRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_count_each_registered_event_once
  // ---------------------------------------------------------------------------

  @Test
  void should_count_each_registered_event_once() {
    Payroll payroll = payroll(PayrollStatus.PENDING, money("1500000"), money("0"), money("0"));
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8))
        .thenReturn(Optional.of(payroll));
    when(eventRepository.save(any())).thenAnswer(invocation -> store(invocation.getArgument(0)));
    when(eventRepository.findByUserAndPeriod(any(), any(), any())).thenAnswer(i -> storedEvents);
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());
    when(payrollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    PayrollEvent event =
        PayrollEvent.create(
            USER_ID,
            EVENT_DATE,
            PayrollEventType.BONUS_PERFORMANCE,
            BigDecimal.ONE,
            money("50000"),
            "Bonus",
            "admin");

    service.execute(event);
    service.execute(event);

    ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
    verify(payrollRepository, org.mockito.Mockito.times(2)).save(captor.capture());
    Payroll lastSaved = captor.getAllValues().get(1);
    assertEquals(2, storedEvents.size());
    assertEquals(0, money("100000").compareTo(lastSaved.bonuses()));
  }

  // ---------------------------------------------------------------------------
  // UC-06: should_reject_event_that_breaks_payroll_invariants
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_event_that_breaks_payroll_invariants() {
    Payroll payroll = payroll(PayrollStatus.PENDING, money("1000000"), money("0"), money("0"));
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, 2026, 8))
        .thenReturn(Optional.of(payroll));
    when(eventRepository.findByUserAndPeriod(any(), any(), any())).thenReturn(List.of());
    when(settlementRepository.findByPayrollId(1L)).thenReturn(List.of());

    assertThrows(
        aros.services.rms.core.payroll.domain.exception.PayrollAggregationException.class,
        () ->
            service.execute(
                PayrollEvent.create(
                    USER_ID,
                    EVENT_DATE,
                    PayrollEventType.DEDUCTION,
                    BigDecimal.ONE,
                    money("1200000"),
                    "Too large",
                    "admin")));

    verify(eventRepository, never()).save(any());
  }

  private PayrollEvent store(PayrollEvent event) {
    PayrollEvent persisted =
        new PayrollEvent(
            (long) storedEvents.size() + 1,
            event.userId(),
            event.eventDate(),
            event.eventType(),
            event.quantity(),
            event.unitRate(),
            event.amount(),
            event.notes(),
            Instant.now(),
            event.createdBy());
    storedEvents.add(persisted);
    return persisted;
  }

  private Payroll payroll(
      PayrollStatus status, Money baseSalary, Money manualBonuses, Money manualDeductions) {
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
