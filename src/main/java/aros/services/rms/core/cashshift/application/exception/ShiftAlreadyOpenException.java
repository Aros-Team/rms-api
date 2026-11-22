/* (C) 2026 */

package aros.services.rms.core.cashshift.application.exception;

/** Thrown when opening a shift while another one is open. Maps to HTTP 409. */
public class ShiftAlreadyOpenException extends RuntimeException {

  /** Creates the exception. */
  public ShiftAlreadyOpenException() {
    super("A cash shift is already open; close it before opening another one");
  }
}
