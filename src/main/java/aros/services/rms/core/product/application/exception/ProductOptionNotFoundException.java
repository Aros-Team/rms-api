/* (C) 2026 */
package aros.services.rms.core.product.application.exception;

/** Exception thrown when a product option is not found by its identifier. */
public class ProductOptionNotFoundException extends RuntimeException {

  public ProductOptionNotFoundException(Long id) {
    super("Product option not found: " + id);
  }
}
