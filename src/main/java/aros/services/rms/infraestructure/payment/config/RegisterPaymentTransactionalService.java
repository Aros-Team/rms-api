/* (C) 2026 */

package aros.services.rms.infraestructure.payment.config;

import aros.services.rms.core.payment.application.dto.RegisterPaymentCommand;
import aros.services.rms.core.payment.application.service.RegisterPaymentService;
import aros.services.rms.core.payment.domain.RegisteredPayment;
import aros.services.rms.core.payment.port.input.RegisterPaymentUseCase;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional entry point for a charge.
 *
 * <p>Keeping the transaction here (and not in the core service) lets the order row lock, the
 * idempotency lookup and the insert share one transaction without adding Spring to the domain.
 */
@Service
@Primary
public class RegisterPaymentTransactionalService implements RegisterPaymentUseCase {

  private final RegisterPaymentService delegate;

  /**
   * Creates the wrapper.
   *
   * @param delegate the framework-free charge service
   */
  public RegisterPaymentTransactionalService(RegisterPaymentService delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public RegisteredPayment register(RegisterPaymentCommand command) {
    return delegate.register(command);
  }
}
