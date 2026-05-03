/* (C) 2026 */

package aros.services.rms.core.product.application.exception;

/** Exception thrown when a product option is not valid for a specific product. */
public class InvalidProductOptionException extends RuntimeException {
  /**
   * Creates a new exception for invalid product option.
   *
   * @param productId the product identifier
   * @param optionId the option identifier that is not valid
   */
  public InvalidProductOptionException(Long productId, Long optionId) {
    super("Option " + optionId + " is not valid for product " + productId);
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public InvalidProductOptionException(String message) {
    super(message);
  }
}
