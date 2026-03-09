/* (C) 2026 */
package aros.services.rms.infraestructure.auth.exception;

import aros.services.rms.infraestructure.common.exception.ExpectedStartupException;

public class JwtKeysMissingException extends ExpectedStartupException {
  public JwtKeysMissingException() {
    super(
        "JWT keys not configured. Run './gradlew generate-jwt-keys' or 'task jwtkeys' to generate and add to .env file");
  }
}
