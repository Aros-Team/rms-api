/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.exception.PayrollEventNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import lombok.RequiredArgsConstructor;

/** Service implementation for deleting a payroll event. */
@RequiredArgsConstructor
public class DeletePayrollEventService implements DeletePayrollEventUseCase {

  private final PayrollEventRepositoryPort eventRepository;
  private final Logger logger;

  @Override
  public void execute(Long eventId) {
    logger.info("Deleting payroll event {}", eventId);
    if (eventRepository.findById(eventId).isEmpty()) {
      throw new PayrollEventNotFoundException(eventId);
    }
    eventRepository.deleteById(eventId);
    logger.info("Deleted payroll event {}", eventId);
  }
}
