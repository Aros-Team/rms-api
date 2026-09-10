/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import lombok.RequiredArgsConstructor;

/** Service implementation for registering a new payroll event. */
@RequiredArgsConstructor
public class RegisterPayrollEventService implements RegisterPayrollEventUseCase {

  private final PayrollEventRepositoryPort eventRepository;
  private final Logger logger;

  @Override
  public PayrollEvent execute(PayrollEvent event) {
    logger.info("Registering payroll event for user {} on {}", event.userId(), event.eventDate());
    PayrollEvent saved = eventRepository.save(event);
    logger.info("Registered payroll event {} for user {}", saved.id(), saved.userId());
    return saved;
  }
}
