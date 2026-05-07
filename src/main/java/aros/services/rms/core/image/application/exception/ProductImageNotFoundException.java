package aros.services.rms.core.image.application.exception;

/** Thrown when a product image is not found. */
public class ProductImageNotFoundException extends RuntimeException {
  /** Creates exception with image ID. */
  public ProductImageNotFoundException(Long id) {
    super("Product image not found: " + id);
  }
}
