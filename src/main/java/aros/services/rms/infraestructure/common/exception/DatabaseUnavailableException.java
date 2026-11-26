/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

/** Exception thrown when the database is not available. */
public class DatabaseUnavailableException extends ExpectedStartupException {
  /** Creates a new exception. */
  public DatabaseUnavailableException() {
    super(
        "Base de datos no disponible. Verifica que Docker esté en ejecución: "
            + "'docker compose up -d'");
  }
}
