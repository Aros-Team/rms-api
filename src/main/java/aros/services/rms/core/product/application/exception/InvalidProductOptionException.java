/* (C) 2026 */
package aros.services.rms.core.product.application.exception;

public class InvalidProductOptionException extends RuntimeException {
  public InvalidProductOptionException(Long productId, Long optionId) {
    super("Option " + optionId + " is not valid for product " + productId);
  }

  public InvalidProductOptionException(String message) {
    super(message);
  }
}
