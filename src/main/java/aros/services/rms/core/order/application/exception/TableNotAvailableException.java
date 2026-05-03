/* (C) 2026 */

package aros.services.rms.core.order.application.exception;

/** Exception thrown when a table is not available for placing an order. */
public class TableNotAvailableException extends RuntimeException {

  /**
   * Creates a new exception for table not available.
   *
   * @param tableId the table identifier that is not available
   */
  public TableNotAvailableException(Long tableId) {
    super("Table not available: " + tableId);
  }

  /**
   * Creates a new exception for table not available with reason.
   *
   * @param tableId the table identifier that is not available
   * @param reason the reason why the table is not available
   */
  public TableNotAvailableException(Long tableId, String reason) {
    super("Table not available: " + tableId + " - " + reason);
  }
}
