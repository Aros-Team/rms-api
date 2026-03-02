package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;

public interface AddProductToOrderInput {
    /**
     * Ejecuta el caso de uso para agregar un producto a un pedido.
     * * @param orderId   ID del pedido actual.
     * @param productId ID del producto a agregar.
     * @param quantity  Cantidad solicitada.
     * @param note      Notas o modificadores del cliente.
     * @return El pedido actualizado.
     */
    Order execute(Long orderId, Long productId, Integer quantity, String note);
}