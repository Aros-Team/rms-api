/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Recomputes the derived payroll totals from the persisted events and settlements.
 *
 * <p>This is the single aggregation path used by event and settlement mutations. Because the totals
 * are always rebuilt from the source rows, running it twice with unchanged inputs produces the same
 * result (no double counting) and deleting an event removes exactly that event's contribution.
 */
@RequiredArgsConstructor
public class PayrollAggregationService {

  private final PayrollRepositoryPort payrollRepositoryPort;
  private final PayrollEventRepositoryPort eventRepositoryPort;
  private final PayrollSettlementRepositoryPort settlementRepositoryPort;
  private final Logger logger;

  /**
   * Recomputes bonuses, deductions, netAmount and paidAmount for a payroll and persists it.
   *
   * @param payroll the payroll to re-aggregate
   * @return the saved payroll with the derived totals
   */
  public Payroll recompute(Payroll payroll) {
    Payroll aggregated = preview(payroll, null, null);
    Payroll saved = payrollRepositoryPort.save(aggregated);
    logger.info(
        "Re-aggregated payroll {} for user {} period {}: bonuses={}, deductions={}, "
            + "netAmount={}, paidAmount={}",
        saved.id(),
        saved.userId(),
        saved.period(),
        saved.bonuses(),
        saved.deductions(),
        saved.netAmount(),
        saved.paidAmount());
    return saved;
  }

  /**
   * Computes the totals that would result from a pending event mutation without saving them.
   *
   * <p>Services call this before persisting an event so an aggregation that would break the payroll
   * invariants (negative net or paid above net) is rejected before the event row is written.
   *
   * @param payroll the payroll to evaluate
   * @param addedEvent an event about to be added, or null
   * @param excludedEventId an event about to be deleted, or null
   * @return the payroll with the prospective totals, not persisted
   */
  public Payroll preview(Payroll payroll, PayrollEvent addedEvent, Long excludedEventId) {
    Money zero = Money.zero(payroll.baseSalary().currency());
    Money eventBonuses = zero;
    Money eventDeductions = zero;

    List<PayrollEvent> events =
        eventRepositoryPort.findByUserAndPeriod(
            payroll.userId(), payroll.periodStart(), payroll.periodEnd());
    for (PayrollEvent event : events) {
      if (excludedEventId != null && excludedEventId.equals(event.id())) {
        continue;
      }
      eventBonuses = eventBonuses.plus(contribution(event, payroll, true));
      eventDeductions = eventDeductions.plus(contribution(event, payroll, false));
    }
    if (addedEvent != null) {
      eventBonuses = eventBonuses.plus(contribution(addedEvent, payroll, true));
      eventDeductions = eventDeductions.plus(contribution(addedEvent, payroll, false));
    }

    Money paid = zero;
    List<PayrollSettlement> settlements = settlementRepositoryPort.findByPayrollId(payroll.id());
    for (PayrollSettlement settlement : settlements) {
      paid = paid.plus(settlement.amount());
    }

    return payroll.reaggregate(eventBonuses, eventDeductions, paid, Instant.now());
  }

  private Money contribution(PayrollEvent event, Payroll payroll, boolean earning) {
    if (!payroll.userId().equals(event.userId()) || !payroll.covers(event.eventDate())) {
      return Money.zero(payroll.baseSalary().currency());
    }
    return earning ? event.earningAmount() : event.deductionAmount();
  }
}
