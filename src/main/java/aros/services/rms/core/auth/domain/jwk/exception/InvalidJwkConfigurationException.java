/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk.exception;

/** Thrown when JWK configuration is invalid. */
public class InvalidJwkConfigurationException extends RuntimeException {
  /** Creates a new exception with a descriptive message. */
  public InvalidJwkConfigurationException(String message) {
    super(message);
  }

  /** Creates a new exception with a descriptive message and cause. */
  public InvalidJwkConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
