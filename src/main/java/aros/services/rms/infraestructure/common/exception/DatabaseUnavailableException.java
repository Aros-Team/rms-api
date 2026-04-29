/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

/** Exception thrown when the database is not available. */
public class DatabaseUnavailableException extends ExpectedStartupException {
  /** Creates a new exception. */
  public DatabaseUnavailableException() {
    super("Database not available. Ensure Docker is running: 'docker compose up -d'");
  }
}
