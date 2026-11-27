/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/** Thrown when a payroll record cannot be found by id. */
public class PayrollNotFoundException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param id the payroll id
   */
  public PayrollNotFoundException(Long id) {
    super("Nómina no encontrada con id: " + id);
  }

  /**
   * Creates the exception with a custom message.
   *
   * @param message the detail message
   */
  public PayrollNotFoundException(String message) {
    super(message);
  }
}
