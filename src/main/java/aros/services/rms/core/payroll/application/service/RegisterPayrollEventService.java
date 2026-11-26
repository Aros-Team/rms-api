/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.exception.InvalidPayrollPeriodException;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;

/**
 * Service implementation for registering a new payroll event.
 *
 * <p>An event is only accepted when its worker and date belong to an existing, still mutable
 * payroll. After the event is persisted the payroll is re-aggregated from the full event set.
 */
@RequiredArgsConstructor
public class RegisterPayrollEventService implements RegisterPayrollEventUseCase {

  private final PayrollEventRepositoryPort eventRepository;
  private final PayrollRepositoryPort payrollRepository;
  private final PayrollAggregationService aggregationService;
  private final Logger logger;

  @Override
  public PayrollEvent execute(PayrollEvent event) {
    if (event.userId() == null || event.eventDate() == null) {
      throw new InvalidPayrollPeriodException("userId y eventDate son obligatorios");
    }

    YearMonth period = YearMonth.from(event.eventDate());
    Payroll payroll =
        payrollRepository
            .findByUserIdAndPeriod(event.userId(), period.getYear(), period.getMonthValue())
            .orElseThrow(
                () ->
                    new PayrollNotFoundException(
                        "No hay nómina para el usuario "
                            + event.userId()
                            + " en el periodo "
                            + period
                            + "; register the payroll before adding events"));

    if (payroll.isImmutable()) {
      throw new PayrollImmutableException(payroll.id(), payroll.status().name());
    }
    if (!payroll.covers(event.eventDate())) {
      throw new InvalidPayrollPeriodException(
          "La fecha del evento "
              + event.eventDate()
              + " está fuera del periodo de nómina "
              + payroll.periodStart()
              + ".."
              + payroll.periodEnd());
    }

    logger.info("Registering payroll event for user {} on {}", event.userId(), event.eventDate());
    aggregationService.preview(payroll, event, null);
    PayrollEvent saved = eventRepository.save(event);
    aggregationService.recompute(payroll);
    logger.info("Registered payroll event {} for user {}", saved.id(), saved.userId());
    return saved;
  }
}
