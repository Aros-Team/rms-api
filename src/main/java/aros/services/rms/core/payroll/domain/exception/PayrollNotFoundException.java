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
    super("Payroll not found with id: " + id);
  }
}
