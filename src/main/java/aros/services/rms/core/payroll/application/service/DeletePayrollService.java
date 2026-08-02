/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import lombok.RequiredArgsConstructor;

/** Service implementation for deleting a payroll record. */
@RequiredArgsConstructor
public class DeletePayrollService implements DeletePayrollUseCase {

  private final PayrollRepositoryPort payrollRepositoryPort;
  private final Logger logger;

  @Override
  public void delete(Long id) {
    Payroll payroll =
        payrollRepositoryPort.findById(id).orElseThrow(() -> new PayrollNotFoundException(id));

    if (!payroll.isPending()) {
      throw new PayrollImmutableException(id, payroll.status().name());
    }

    payrollRepositoryPort.deleteById(id);
    logger.info("Deleted payroll {}", id);
  }
}
