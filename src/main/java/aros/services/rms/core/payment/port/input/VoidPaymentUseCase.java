/* (C) 2026 */

package aros.services.rms.core.payment.port.input;

import aros.services.rms.core.payment.application.dto.VoidPaymentCommand;
import aros.services.rms.core.payment.domain.Payment;

/** Use case for voiding a completed payment. */
public interface VoidPaymentUseCase {

  /**
   * Voids a completed payment while its shift is open.
   *
   * @param command the void command, including the mandatory reason
   * @return the voided payment
   */
  Payment voidPayment(VoidPaymentCommand command);
}
