/* (C) 2026 */

package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderQueryResult;
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

  /**
   * Busca órdenes con paginación y filtros opcionales.
   *
   * @param statuses lista de estados de orden (opcional, vacío = sin filtro)
   * @param startDate fecha inicio (opcional)
   * @param endDate fecha fin (opcional)
   * @param page número de página (0-based)
   * @param size tamaño de página
   * @param sort campo y dirección de ordenamiento (ej. "date,desc")
   * @return resultado paginado
   */
  OrderQueryResult findOrdersPage(
      List<OrderStatus> statuses,
      LocalDateTime startDate,
      LocalDateTime endDate,
      int page,
      int size,
      String sort);
}
