package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.application.usecases.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;

/**
 * Caso de uso para modificar órdenes existentes.
 * Permite cancelar o actualizar detalles de una orden.
 */
public interface UpdateOrderUseCase {

    /**
     * Cancela una orden.
     *
     * @param id ID de la orden a cancelar
     * @return Orden cancelada
     * @throws IllegalArgumentException si no existe la orden
     */
    Order cancel(Long id);

    /**
     * Actualiza los detalles de una orden.
     *
     * @param id ID de la orden a actualizar
     * @param command Nuevos datos de la orden
     * @return Orden actualizada
     * @throws IllegalArgumentException si no existe la orden o productos
     * @throws IllegalStateException si la orden no puede modificarse
     */
    Order update(Long id, TakeOrderCommand command);
}