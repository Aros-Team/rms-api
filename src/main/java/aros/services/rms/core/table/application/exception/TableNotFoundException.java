/* (C) 2026 */
package aros.services.rms.core.table.application.exception;

/** Exception thrown when a table is not found by its identifier. */
public class TableNotFoundException extends RuntimeException {

  public TableNotFoundException(Long id) {
    super("Table not found: " + id);
  }
}
