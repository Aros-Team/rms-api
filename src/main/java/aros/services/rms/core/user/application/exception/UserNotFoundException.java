/* (C) 2026 */
package aros.services.rms.core.user.application.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
