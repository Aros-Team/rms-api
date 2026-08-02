/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.application.service.DeletePayrollService;
import aros.services.rms.core.payroll.application.service.GetPayrollService;
import aros.services.rms.core.payroll.application.service.ListPayrollsService;
import aros.services.rms.core.payroll.application.service.RegisterPayrollService;
import aros.services.rms.core.payroll.application.service.UpdatePayrollService;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.GetPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the payroll module. Registers all payroll use cases. */
@Configuration
public class PayrollConfigBeans {

  /** Creates bean for payroll registration use case. */
  @Bean
  public RegisterPayrollUseCase registerPayrollUseCase(
      PayrollRepositoryPort payrollRepositoryPort, Logger logger) {
    return new RegisterPayrollService(payrollRepositoryPort, logger);
  }

  /** Creates bean for payroll update use case. */
  @Bean
  public UpdatePayrollUseCase updatePayrollUseCase(
      PayrollRepositoryPort payrollRepositoryPort, Logger logger) {
    return new UpdatePayrollService(payrollRepositoryPort, logger);
  }

  /** Creates bean for payroll query use case. */
  @Bean
  public GetPayrollUseCase getPayrollUseCase(PayrollRepositoryPort payrollRepositoryPort) {
    return new GetPayrollService(payrollRepositoryPort);
  }

  /** Creates bean for payroll listing use case. */
  @Bean
  public ListPayrollsUseCase listPayrollsUseCase(PayrollRepositoryPort payrollRepositoryPort) {
    return new ListPayrollsService(payrollRepositoryPort);
  }

  /** Creates bean for payroll deletion use case. */
  @Bean
  public DeletePayrollUseCase deletePayrollUseCase(
      PayrollRepositoryPort payrollRepositoryPort, Logger logger) {
    return new DeletePayrollService(payrollRepositoryPort, logger);
  }
}
