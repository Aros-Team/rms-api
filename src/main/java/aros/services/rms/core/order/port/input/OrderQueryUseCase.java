/* (C) 2026 */

package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

/** Caso de uso para consultar órdenes. Permite filtrar por estado y rango de fechas. */
public interface OrderQueryUseCase {

  /**
   * Busca órdenes aplicando filtros opcionales.
   *
   * @param status Estado de la orden (opcional)
   * @param startDate Fecha inicio (opcional)
   * @param endDate Fecha fin (opcional)
   * @return Lista de órdenes que cumplen los filtros
   */
  List<Order> findOrders(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
