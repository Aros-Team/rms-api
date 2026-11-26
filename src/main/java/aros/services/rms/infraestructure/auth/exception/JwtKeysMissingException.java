/* (C) 2026 */

package aros.services.rms.infraestructure.auth.exception;

import aros.services.rms.infraestructure.common.exception.ExpectedStartupException;

/** Exception thrown when JWT keys are not configured. */
public class JwtKeysMissingException extends ExpectedStartupException {
  /** Creates a new exception. */
  public JwtKeysMissingException() {
    super(
        "Claves JWT no configuradas. Ejecuta './gradlew generate-jwt-keys' o 'task jwtkeys' "
            + "to generate and add to .env file");
  }
}
