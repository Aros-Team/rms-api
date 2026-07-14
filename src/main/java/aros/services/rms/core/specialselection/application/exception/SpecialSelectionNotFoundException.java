package aros.services.rms.core.specialselection.application.exception;

/** Exception raised when a special selection cannot be found for the given product identifier. */
public class SpecialSelectionNotFoundException extends RuntimeException {
  /**
   * Creates a new special selection not found exception.
   *
   * @param productId the product identifier
   */
  public SpecialSelectionNotFoundException(Long productId) {
    super("Special selection not found: " + productId);
  }
}
