/* (C) 2026 */

package aros.services.rms.core.cashshift.application.exception;

/** Thrown when a cash shift does not exist. Maps to HTTP 404. */
public class CashShiftNotFoundException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param id the missing shift id
   */
  public CashShiftNotFoundException(Long id) {
    super("Cash shift not found: " + id);
  }
}
