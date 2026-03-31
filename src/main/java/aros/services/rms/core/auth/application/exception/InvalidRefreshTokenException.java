package aros.services.rms.core.auth.application.exception;

public class InvalidRefreshTokenException extends Exception {
  public InvalidRefreshTokenException() {
    super();
  }

  public InvalidRefreshTokenException(String message) {
    super(message);
  }
}
