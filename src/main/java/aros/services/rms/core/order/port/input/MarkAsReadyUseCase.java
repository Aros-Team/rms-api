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
   * <p>Todas las líneas de la orden quedan marcadas como listas para que la vista por ítem y la
   * vista por orden coincidan.
   *
   * @param id ID de la orden a marcar
   * @return Orden actualizada con estado READY
   * @throws IllegalArgumentException si no existe la orden
   * @throws IllegalStateException si la orden no está en PREPARING
   */
  Order markAsReady(Long id);

  /**
   * Marca una línea de la orden como lista en su estación. Cuando todas las líneas quedan listas,
   * la orden pasa a READY. Es idempotente para una línea ya lista.
   *
   * @param orderId ID de la orden
   * @param detailId ID de la línea
   * @return Orden actualizada (con estado READY si todas las líneas quedaron listas)
   * @throws aros.services.rms.core.order.application.exception.OrderNotFoundException si no existe
   *     la orden
   * @throws aros.services.rms.core.order.application.exception.OrderDetailNotFoundException si la
   *     línea no pertenece a la orden
   * @throws IllegalStateException si la orden no está en PREPARING o READY
   */
  Order markDetailAsReady(Long orderId, Long detailId);

  /**
   * Marks every pending line of the given preparation area as ready.
   *
   * @param orderId the order identifier
   * @param areaId the preparation area identifier
   * @return the updated order
   */
  Order markAreaAsReady(Long orderId, Long areaId);
}
