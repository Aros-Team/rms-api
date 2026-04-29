/* (C) 2026 */
package aros.services.rms.infraestructure.auth.exception;

import aros.services.rms.infraestructure.common.exception.ExpectedStartupException;

/** Exception thrown when JWT keys are not configured. */
public class JwtKeysMissingException extends ExpectedStartupException {
  /** Creates a new exception. */
  public JwtKeysMissingException() {
    super(
        "JWT keys not configured. Run './gradlew generate-jwt-keys' or 'task jwtkeys' to generate and add to .env file");
  }
}
