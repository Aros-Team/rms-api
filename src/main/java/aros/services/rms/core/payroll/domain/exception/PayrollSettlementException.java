/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/** Exception thrown when a payroll settlement operation fails. */
public class PayrollSettlementException extends RuntimeException {

  /**
   * Creates the exception with the given message.
   *
   * @param message the error message
   */
  public PayrollSettlementException(String message) {
    super(message);
  }
}
