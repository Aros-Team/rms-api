/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk.exception;

/** Thrown when a requested JWK key is not found. */
public class JwkKeyNotFoundException extends RuntimeException {
  /** Creates a new exception with a descriptive message. */
  public JwkKeyNotFoundException(String message) {
    super(message);
  }
}
