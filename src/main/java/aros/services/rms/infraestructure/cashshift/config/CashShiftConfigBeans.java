/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.config;

import aros.services.rms.core.cashshift.application.service.CashShiftQueryService;
import aros.services.rms.core.cashshift.application.service.CloseCashShiftService;
import aros.services.rms.core.cashshift.application.service.OpenCashShiftService;
import aros.services.rms.core.cashshift.port.input.CashShiftQueryUseCase;
import aros.services.rms.core.cashshift.port.output.CashMovementRepositoryPort;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring beans for the framework-free cash-shift services. */
@Configuration
public class CashShiftConfigBeans {

  /**
   * Provides the open-shift use case implementation.
   *
   * @param cashShiftRepositoryPort cash-shift persistence port
   * @return the open service
   */
  @Bean
  public OpenCashShiftService openCashShiftService(
      CashShiftRepositoryPort cashShiftRepositoryPort) {
    return new OpenCashShiftService(cashShiftRepositoryPort);
  }

  /**
   * Provides the close-shift use case implementation.
   *
   * @param cashShiftRepositoryPort cash-shift persistence port
   * @param paymentRepositoryPort payment persistence port
   * @param cashMovementRepositoryPort movement persistence port
   * @return the close service
   */
  @Bean
  public CloseCashShiftService closeCashShiftService(
      CashShiftRepositoryPort cashShiftRepositoryPort,
      PaymentRepositoryPort paymentRepositoryPort,
      CashMovementRepositoryPort cashMovementRepositoryPort) {
    return new CloseCashShiftService(
        cashShiftRepositoryPort, paymentRepositoryPort, cashMovementRepositoryPort);
  }

  /**
   * Provides the cash-shift read use case.
   *
   * @param cashShiftRepositoryPort cash-shift persistence port
   * @param paymentRepositoryPort payment persistence port
   * @param cashMovementRepositoryPort movement persistence port
   * @return the query service
   */
  @Bean
  public CashShiftQueryUseCase cashShiftQueryUseCase(
      CashShiftRepositoryPort cashShiftRepositoryPort,
      PaymentRepositoryPort paymentRepositoryPort,
      CashMovementRepositoryPort cashMovementRepositoryPort) {
    return new CashShiftQueryService(
        cashShiftRepositoryPort, paymentRepositoryPort, cashMovementRepositoryPort);
  }
}
