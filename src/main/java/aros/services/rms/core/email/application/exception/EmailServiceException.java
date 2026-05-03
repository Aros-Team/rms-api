/* (C) 2026 */

package aros.services.rms.core.email.application.exception;

/** Exception thrown when email sending fails. */
public class EmailServiceException extends RuntimeException {

  /**
   * Creates an email service exception.
   *
   * @param message error message
   */
  public EmailServiceException(String message) {
    super(message);
  }

  /**
   * Creates an email service exception with cause.
   *
   * @param message error message
   * @param cause the cause
   */
  public EmailServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
