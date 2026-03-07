/* (C) 2026 */
package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;

/**
 * Caso de uso para marcar una orden como lista para entregar. Permite al cocinero indicar que
 * terminó la preparación.
 */
public interface MarkAsReadyUseCase {

  /**
   * Marca una orden específica como READY. Solo puede usarse en órdenes en estado PREPARING.
   *
   * @param id ID de la orden a marcar
   * @return Orden actualizada con estado READY
   * @throws IllegalArgumentException si no existe la orden
   * @throws IllegalStateException si la orden no está en PREPARING
   */
  Order markAsReady(Long id);
}
