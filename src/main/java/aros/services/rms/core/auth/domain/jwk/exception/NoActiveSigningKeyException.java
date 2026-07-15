/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk.exception;

/** Thrown when no active signing key is available for JWK publishing. */
public class NoActiveSigningKeyException extends RuntimeException {
  /** Creates a new exception with no message. */
  public NoActiveSigningKeyException() {
    super();
  }

  /** Creates a new exception with a descriptive message. */
  public NoActiveSigningKeyException(String message) {
    super(message);
  }
}
