/* (C) 2026 */
package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;

/**
 * Caso de uso para crear nuevas órdenes. Valida disponibilidad de mesa y productos seleccionados.
 */
public interface TakeOrderUseCase {

  /**
   * Crea una nueva orden en estado QUEUE.
   *
   * @param command Datos de la orden a crear
   * @return Orden creada
   * @throws IllegalArgumentException si mesa o productos no existen
   * @throws IllegalStateException si la mesa no está disponible
   */
  Order execute(TakeOrderCommand command);
}
