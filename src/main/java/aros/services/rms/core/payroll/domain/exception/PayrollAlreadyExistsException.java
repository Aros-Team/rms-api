/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/**
 * Thrown when attempting to register a payroll that already exists for the given user and period.
 */
public class PayrollAlreadyExistsException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param userId the user id
   * @param year the year
   * @param month the month
   */
  public PayrollAlreadyExistsException(Long userId, int year, int month) {
    super("Payroll already exists for user " + userId + " in " + year + "-" + month);
  }
}
