package aros.services.rms.core.order.application.exception;

public class TableNotAvailableException extends RuntimeException {

  public TableNotAvailableException(Long tableId) {
    super("Table not available: " + tableId);
  }

  public TableNotAvailableException(Long tableId, String reason) {
    super("Table not available: " + tableId + " - " + reason);
  }
}
