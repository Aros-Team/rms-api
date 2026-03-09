/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

public class MissingEnvVariableException extends ExpectedStartupException {

  public MissingEnvVariableException(String variableName) {
    super("Environment variable '" + variableName + "' is not set. Add it to your .env file");
  }
}
