/* (C) 2026 */

package aros.services.rms.core.cashshift.application.exception;

/** Thrown when an operation requires an open shift and none exists. Maps to HTTP 409. */
public class ShiftNotOpenException extends RuntimeException {

  /** Creates the exception. */
  public ShiftNotOpenException() {
    super("No hay un turno de caja abierto; abre la caja antes de cobrar");
  }
}
