/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.exception.PayrollEventNotFoundException;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import java.time.YearMonth;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Service implementation for deleting a payroll event.
 *
 * <p>The payroll is re-aggregated after the row is deleted, so only that event's contribution is
 * removed even if the same payroll was aggregated several times before.
 */
@RequiredArgsConstructor
public class DeletePayrollEventService implements DeletePayrollEventUseCase {

  private final PayrollEventRepositoryPort eventRepository;
  private final PayrollRepositoryPort payrollRepository;
  private final PayrollAggregationService aggregationService;
  private final Logger logger;

  @Override
  public void execute(Long eventId) {
    logger.info("Deleting payroll event {}", eventId);
    PayrollEvent event =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new PayrollEventNotFoundException(eventId));

    YearMonth period = YearMonth.from(event.eventDate());
    Optional<Payroll> payroll =
        payrollRepository.findByUserIdAndPeriod(
            event.userId(), period.getYear(), period.getMonthValue());

    if (payroll.isPresent()) {
      if (payroll.get().isImmutable()) {
        throw new PayrollImmutableException(payroll.get().id(), payroll.get().status().name());
      }
      aggregationService.preview(payroll.get(), null, eventId);
    }

    eventRepository.deleteById(eventId);
    payroll.ifPresent(aggregationService::recompute);
    logger.info("Deleted payroll event {}", eventId);
  }
}
