package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;

public interface AddProductToOrderInput {
    /**
     * Agrega un producto a un pedido existente.
     * @param orderId   Identificador del pedido.
     * @param productId Identificador del producto del menú.
     * @param quantity  Cantidad a agregar.
     * @param note      Notas para la cocina (ej. "sin cebolla").
     * @return El pedido actualizado con los nuevos totales.
     */
    Order execute(Long orderId, Long productId, Integer quantity, String note);
}

