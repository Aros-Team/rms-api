/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

/** Exception thrown when a service is unavailable. */
public class ServiceUnavailableException extends RuntimeException {

  /** Creates a new exception. */
  public ServiceUnavailableException(String message) {
    super(message);
  }

  /** Creates a new exception with cause. */
  public ServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
