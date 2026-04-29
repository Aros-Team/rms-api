/* (C) 2026 */

package aros.services.rms.core.order.application.exception;

/** Exception thrown when a product is not found during order processing. */
public class ProductNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for missing product.
   *
   * @param productId the product identifier that was not found
   */
  public ProductNotFoundException(Long productId) {
    super("Product not found: " + productId);
  }
}
