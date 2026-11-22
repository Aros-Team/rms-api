/* (C) 2026 */

package aros.services.rms.infraestructure.payment.config;

import aros.services.rms.core.payment.application.dto.VoidPaymentCommand;
import aros.services.rms.core.payment.application.service.VoidPaymentService;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.port.input.VoidPaymentUseCase;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional entry point for a payment void. */
@Service
@Primary
public class VoidPaymentTransactionalService implements VoidPaymentUseCase {

  private final VoidPaymentService delegate;

  /**
   * Creates the wrapper.
   *
   * @param delegate the framework-free void service
   */
  public VoidPaymentTransactionalService(VoidPaymentService delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Payment voidPayment(VoidPaymentCommand command) {
    return delegate.voidPayment(command);
  }
}
