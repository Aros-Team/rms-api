/* (C) 2026 */
package aros.services.rms.core.user.application.exception;

public class UserDeletedException extends RuntimeException {
  public UserDeletedException() {
    super("Tu cuenta ha sido eliminada del sistema");
  }

  public UserDeletedException(String message) {
    super(message);
  }
}