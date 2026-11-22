/* (C) 2026 */

package aros.services.rms.core.payment.port.input;

import aros.services.rms.core.payment.application.dto.RegisterPaymentCommand;
import aros.services.rms.core.payment.domain.RegisteredPayment;

/** Use case for charging a delivered order. */
public interface RegisterPaymentUseCase {

  /**
   * Registers the payment of an order inside the currently open cash shift.
   *
   * @param command the charge command
   * @return the stored payment and whether it was an idempotent replay
   */
  RegisteredPayment register(RegisterPaymentCommand command);
}
