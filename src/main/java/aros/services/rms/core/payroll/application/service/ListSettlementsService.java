/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.port.input.ListSettlementsUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Service implementation for listing settlements by user and date range. */
@RequiredArgsConstructor
public class ListSettlementsService implements ListSettlementsUseCase {

  private final PayrollSettlementRepositoryPort settlementRepository;
  private final Logger logger;

  @Override
  public List<PayrollSettlement> execute(Long userId, LocalDate startDate, LocalDate endDate) {
    logger.debug("Listing settlements for user {} from {} to {}", userId, startDate, endDate);
    return settlementRepository.findByUserAndPeriod(userId, startDate, endDate);
  }
}
