package aros.services.rms.core.order.application.exception;

public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(Long productId) {
    super("Product not found: " + productId);
  }
}
