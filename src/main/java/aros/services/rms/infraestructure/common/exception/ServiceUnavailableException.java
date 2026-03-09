/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

public class ServiceUnavailableException extends RuntimeException {

  public ServiceUnavailableException(String message) {
    super(message);
  }

  public ServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
