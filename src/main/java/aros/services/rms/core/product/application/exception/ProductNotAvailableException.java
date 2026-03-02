package aros.services.rms.core.product.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductNotAvailableException extends RuntimeException {
    public ProductNotAvailableException(Long id) {
        super("El producto con ID " + id + " no está disponible o no tiene stock suficiente.");
    }
}
