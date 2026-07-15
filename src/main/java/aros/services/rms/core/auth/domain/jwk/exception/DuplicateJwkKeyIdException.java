/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk.exception;

/** Thrown when a JWK key ID is duplicated. */
public class DuplicateJwkKeyIdException extends RuntimeException {
  /** Creates a new exception with a descriptive message. */
  public DuplicateJwkKeyIdException(String message) {
    super(message);
  }
}
