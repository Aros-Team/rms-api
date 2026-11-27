/* (C) 2026 */

package aros.services.rms.core.cashshift.application.exception;

/** Thrown when mutating a closed shift. Maps to HTTP 409. */
public class ShiftClosedException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param shiftId the closed shift
   */
  public ShiftClosedException(Long shiftId) {
    super("El turno de caja " + shiftId + " está cerrado y no se puede modificar");
  }
}
