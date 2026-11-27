/* (C) 2026 */

package aros.services.rms.core.payment.application.exception;

/** Thrown when a payment does not exist. Maps to HTTP 404. */
public class PaymentNotFoundException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param id the missing payment id
   */
  public PaymentNotFoundException(Long id) {
    super("Pago no encontrado: " + id);
  }
}
