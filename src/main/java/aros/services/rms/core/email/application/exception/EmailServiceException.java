/* (C) 2026 */
package aros.services.rms.core.email.application.exception;

public class EmailServiceException extends RuntimeException {

  public EmailServiceException(String message) {
    super(message);
  }

  public EmailServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
