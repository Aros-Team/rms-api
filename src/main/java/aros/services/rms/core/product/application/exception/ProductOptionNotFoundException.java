/* (C) 2026 */

package aros.services.rms.core.product.application.exception;

/** Exception thrown when a product option is not found by its identifier. */
public class ProductOptionNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for missing product option.
   *
   * @param id the product option identifier that was not found
   */
  public ProductOptionNotFoundException(Long id) {
    super("Product option not found: " + id);
  }
}
