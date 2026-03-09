/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

public class DatabaseUnavailableException extends ExpectedStartupException {
  public DatabaseUnavailableException() {
    super("Database not available. Ensure Docker is running: 'docker compose up -d'");
  }
}
