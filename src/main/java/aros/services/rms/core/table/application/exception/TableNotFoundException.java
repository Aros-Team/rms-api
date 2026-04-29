/* (C) 2026 */

package aros.services.rms.core.table.application.exception;

/** Exception thrown when a table is not found by its identifier. */
public class TableNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for missing table.
   *
   * @param id the table identifier that was not found
   */
  public TableNotFoundException(Long id) {
    super("Table not found: " + id);
  }
}
