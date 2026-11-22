/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/** Thrown when a payroll re-aggregation would leave the record in an inconsistent state. */
public class PayrollAggregationException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param message the error message
   */
  public PayrollAggregationException(String message) {
    super(message);
  }
}
