/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollsUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Service implementation for listing payroll records. */
@RequiredArgsConstructor
public class ListPayrollsService implements ListPayrollsUseCase {

  private final PayrollRepositoryPort payrollRepositoryPort;

  @Override
  public List<Payroll> findAll() {
    return payrollRepositoryPort.findAll();
  }

  @Override
  public List<Payroll> findByPeriod(int year, int month) {
    return payrollRepositoryPort.findByPeriod(year, month);
  }

  @Override
  public List<Payroll> findByUserId(Long userId) {
    return payrollRepositoryPort.findByUserId(userId);
  }
}
