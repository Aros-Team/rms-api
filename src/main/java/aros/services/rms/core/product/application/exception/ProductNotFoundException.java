package aros.services.rms.core.product.application.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("No se encontró el producto con ID: " + id);
    }
}