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
}
