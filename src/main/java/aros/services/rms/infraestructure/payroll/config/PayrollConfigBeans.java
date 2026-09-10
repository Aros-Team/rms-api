/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.application.service.CalculatePayrollEventService;
import aros.services.rms.core.payroll.application.service.DeletePayrollEventService;
import aros.services.rms.core.payroll.application.service.DeletePayrollService;
import aros.services.rms.core.payroll.application.service.GetPayrollService;
import aros.services.rms.core.payroll.application.service.ListPayrollEventsService;
import aros.services.rms.core.payroll.application.service.ListPayrollsService;
import aros.services.rms.core.payroll.application.service.ListSettlementsService;
import aros.services.rms.core.payroll.application.service.RegisterPayrollEventService;
import aros.services.rms.core.payroll.application.service.RegisterPayrollService;
import aros.services.rms.core.payroll.application.service.RegisterSettlementService;
import aros.services.rms.core.payroll.application.service.UpdatePayrollService;
import aros.services.rms.core.payroll.domain.port.input.CalculatePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.input.DeletePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.GetPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollEventsUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListPayrollsUseCase;
import aros.services.rms.core.payroll.domain.port.input.ListSettlementsUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.input.RegisterSettlementUseCase;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
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

  /** Creates bean for payroll event registration use case. */
  @Bean
  public RegisterPayrollEventUseCase registerPayrollEventUseCase(
      PayrollEventRepositoryPort eventRepository, Logger logger) {
    return new RegisterPayrollEventService(eventRepository, logger);
  }

  /** Creates bean for payroll event listing use case. */
  @Bean
  public ListPayrollEventsUseCase listPayrollEventsUseCase(
      PayrollEventRepositoryPort eventRepository, Logger logger) {
    return new ListPayrollEventsService(eventRepository, logger);
  }

  /** Creates bean for payroll event deletion use case. */
  @Bean
  public DeletePayrollEventUseCase deletePayrollEventUseCase(
      PayrollEventRepositoryPort eventRepository, Logger logger) {
    return new DeletePayrollEventService(eventRepository, logger);
  }

  /** Creates bean for payroll event calculation use case. */
  @Bean
  public CalculatePayrollEventUseCase calculatePayrollEventUseCase(
      UserRepositoryPort userRepositoryPort, CurrencyProvider currencyProvider) {
    return new CalculatePayrollEventService(userRepositoryPort, currencyProvider);
  }

  /** Creates bean for settlement registration use case. */
  @Bean
  public RegisterSettlementUseCase registerSettlementUseCase(
      PayrollSettlementRepositoryPort settlementRepository, Logger logger) {
    return new RegisterSettlementService(settlementRepository, logger);
  }

  /** Creates bean for settlement listing use case. */
  @Bean
  public ListSettlementsUseCase listSettlementsUseCase(
      PayrollSettlementRepositoryPort settlementRepository, Logger logger) {
    return new ListSettlementsService(settlementRepository, logger);
  }
}
