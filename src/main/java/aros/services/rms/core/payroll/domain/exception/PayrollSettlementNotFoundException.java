/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/** Exception thrown when a payroll settlement cannot be found. */
public class PayrollSettlementNotFoundException extends RuntimeException {

  /**
   * Creates the exception with the given settlement id.
   *
   * @param id the settlement id that was not found
   */
  public PayrollSettlementNotFoundException(Long id) {
    super("Liquidación de nómina no encontrada con id: " + id);
  }
}
