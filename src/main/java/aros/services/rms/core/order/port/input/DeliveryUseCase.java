/* (C) 2026 */

package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;

/**
 * Caso de uso para entregar órdenes al cliente. Marca la orden como entregada y libera recursos
 * asociados.
 */
public interface DeliveryUseCase {

  /**
   * Entrega una orden al cliente (READY -> DELIVERED).
   *
   * @param id ID de la orden a entregar
   * @return Orden entregada
   * @throws IllegalArgumentException si no existe la orden
   * @throws IllegalStateException si la orden no está en READY
   */
  Order markAsDelivered(Long id);
}
