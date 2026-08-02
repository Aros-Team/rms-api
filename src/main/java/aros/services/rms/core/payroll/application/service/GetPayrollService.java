/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.port.input.GetPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/** Service implementation for querying payroll records. */
@RequiredArgsConstructor
public class GetPayrollService implements GetPayrollUseCase {

  private final PayrollRepositoryPort payrollRepositoryPort;

  @Override
  public Optional<Payroll> findByUserAndPeriod(Long userId, int year, int month) {
    return payrollRepositoryPort.findByUserIdAndPeriod(userId, year, month);
  }

  @Override
  public Optional<Payroll> findById(Long id) {
    return payrollRepositoryPort.findById(id);
  }
}
