/* (C) 2026 */

package aros.services.rms.core.order.port.output;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output port for Order persistence operations. */
public interface OrderRepositoryPort {
  /**
   * Saves an order to the repository.
   *
   * @param order the order to save
   * @return the saved order with generated ID
   */
  Order save(Order order);

  /**
   * Finds an order by its identifier.
   *
   * @param id the order identifier
   * @return Optional containing the order if found
   */
  Optional<Order> findById(Long id);

  /**
   * Finds the first order by status ordered by date ascending.
   *
   * @param status the order status to search
   * @return Optional containing the first order if found
   */
  Optional<Order> findFirstByStatusOrderByDateAsc(OrderStatus status);

  /**
   * Retrieves all orders.
   *
   * @return list of all orders
   */
  List<Order> findAll();

  /**
   * Finds all orders with a specific status.
   *
   * @param status the order status to search
   * @return list of orders with the specified status
   */
  List<Order> findByStatus(OrderStatus status);

  /**
   * Finds all orders within a date range.
   *
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   * @return list of orders within the date range
   */
  List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Finds all orders with a specific status within a date range.
   *
   * @param status the order status to search
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   * @return list of orders with the specified status and date range
   */
  List<Order> findByStatusAndDateBetween(
      OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
