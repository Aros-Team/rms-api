/* (C) 2026 */
package aros.services.rms.core.table.application.exception;

/** Exception thrown when an invalid table status transition is attempted. */
public class InvalidTableStatusException extends RuntimeException {

  public InvalidTableStatusException(String message) {
    super(message);
  }
}
