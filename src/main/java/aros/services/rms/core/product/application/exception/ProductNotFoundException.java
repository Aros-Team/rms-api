/* (C) 2026 */

package aros.services.rms.core.product.application.exception;

/** Exception thrown when a product is not found by its identifier. */
public class ProductNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for missing product.
   *
   * @param id the product identifier that was not found
   */
  public ProductNotFoundException(Long id) {
    super("Product not found: " + id);
  }
}
