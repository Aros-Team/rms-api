package aros.services.rms.core.order.application.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("No se encontró el pedido con ID: " + id);
    }
}