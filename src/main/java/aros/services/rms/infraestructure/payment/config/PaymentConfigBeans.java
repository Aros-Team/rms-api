/* (C) 2026 */

package aros.services.rms.infraestructure.payment.config;

import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.payment.application.service.PaymentQueryService;
import aros.services.rms.core.payment.application.service.RegisterPaymentService;
import aros.services.rms.core.payment.application.service.VoidPaymentService;
import aros.services.rms.core.payment.port.input.PaymentQueryUseCase;
import aros.services.rms.core.payment.port.output.PayableOrderPort;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import aros.services.rms.core.payment.port.output.PendingOrderQueryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring beans for the framework-free payment services. */
@Configuration
public class PaymentConfigBeans {

  /**
   * Provides the charge use case implementation.
   *
   * @param payableOrderPort order lock and snapshot port
   * @param paymentRepositoryPort payment persistence port
   * @param cashShiftRepositoryPort cash-shift persistence port
   * @return the charge service
   */
  @Bean
  public RegisterPaymentService registerPaymentService(
      PayableOrderPort payableOrderPort,
      PaymentRepositoryPort paymentRepositoryPort,
      CashShiftRepositoryPort cashShiftRepositoryPort) {
    return new RegisterPaymentService(
        payableOrderPort, paymentRepositoryPort, cashShiftRepositoryPort);
  }

  /**
   * Provides the void use case implementation.
   *
   * @param paymentRepositoryPort payment persistence port
   * @param cashShiftRepositoryPort cash-shift persistence port
   * @return the void service
   */
  @Bean
  public VoidPaymentService voidPaymentService(
      PaymentRepositoryPort paymentRepositoryPort,
      CashShiftRepositoryPort cashShiftRepositoryPort) {
    return new VoidPaymentService(paymentRepositoryPort, cashShiftRepositoryPort);
  }

  /**
   * Provides the payment read use case.
   *
   * @param paymentRepositoryPort payment persistence port
   * @param pendingOrderQueryPort pending-orders read model
   * @return the query service
   */
  @Bean
  public PaymentQueryUseCase paymentQueryUseCase(
      PaymentRepositoryPort paymentRepositoryPort, PendingOrderQueryPort pendingOrderQueryPort) {
    return new PaymentQueryService(paymentRepositoryPort, pendingOrderQueryPort);
  }
}
