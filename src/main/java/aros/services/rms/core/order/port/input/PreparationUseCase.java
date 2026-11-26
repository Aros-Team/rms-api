/* (C) 2026 */

package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;

/**
 * Caso de uso para pasar órdenes a preparación. Toma la siguiente orden de la cola y la asigna al
 * cocinero.
 */
public interface PreparationUseCase {

  /**
   * Procesa la siguiente orden en cola (QUEUE -> PREPARING).
   *
   * @return Orden que pasó a preparación
   * @throws IllegalStateException si no hay órdenes en cola
   */
  Order processNextOrder();

  /**
   * Pasa a preparación una orden concreta que esté en cola (QUEUE -> PREPARING).
   *
   * @param id identificador de la orden
   * @return la orden que pasó a preparación
   * @throws aros.services.rms.core.order.application.exception.OrderNotFoundException si no existe
   * @throws IllegalStateException si la orden no está en estado QUEUE
   */
  Order prepareOrder(Long id);

  /**
   * Starts one order line at its station.
   *
   * @param orderId the order identifier
   * @param detailId the line identifier
   * @return the updated order
   */
  Order startDetail(Long orderId, Long detailId);

  /**
   * Starts every pending line of the given preparation area in the order.
   *
   * @param orderId the order identifier
   * @param areaId the preparation area identifier
   * @return the updated order
   */
  Order startArea(Long orderId, Long areaId);
}
