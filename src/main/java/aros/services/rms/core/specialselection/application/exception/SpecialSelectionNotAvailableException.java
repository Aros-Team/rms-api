package aros.services.rms.core.specialselection.application.exception;

/** Exception raised when a special selection is requested outside its availability window. */
public class SpecialSelectionNotAvailableException extends RuntimeException {
  private final Long productId;

  /**
   * Creates a new special selection not available exception.
   *
   * @param productId the product identifier
   */
  public SpecialSelectionNotAvailableException(Long productId) {
    super("Special selection with productId=" + productId + " is not available at this time");
    this.productId = productId;
  }

  public Long getProductId() {
    return productId;
  }
}
