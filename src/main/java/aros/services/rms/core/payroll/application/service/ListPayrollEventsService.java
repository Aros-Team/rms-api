/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollEventsUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Service implementation for listing payroll events by user and date range. */
@RequiredArgsConstructor
public class ListPayrollEventsService implements ListPayrollEventsUseCase {

  private final PayrollEventRepositoryPort eventRepository;
  private final Logger logger;

  @Override
  public List<PayrollEvent> execute(Long userId, LocalDate startDate, LocalDate endDate) {
    logger.debug("Listing payroll events for user {} from {} to {}", userId, startDate, endDate);
    return eventRepository.findByUserAndPeriod(userId, startDate, endDate);
  }
}
