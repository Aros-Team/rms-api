/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

/** Exception thrown when an environment variable is missing. */
public class MissingEnvVariableException extends ExpectedStartupException {

  /** Creates a new exception. */
  public MissingEnvVariableException(String variableName) {
    super(
        "Environment variable '" + variableName + "' no está definida. Agrégala a tu archivo .env");
  }
}
