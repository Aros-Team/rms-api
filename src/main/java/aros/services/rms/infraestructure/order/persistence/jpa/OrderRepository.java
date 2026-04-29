/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.infraestructure.order.persistence.Order;
import aros.services.rms.infraestructure.order.persistence.OrderStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for orders. */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * Finds the first order by status ordered by date ascending with pessimistic lock.
   *
   * @param status the order status
   * @return the first order
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.date ASC LIMIT 1")
  Optional<Order> findFirstByStatusOrderByDateAsc(@Param("status") OrderStatus status);

  /**
   * Finds all orders by status.
   *
   * @param status the order status
   * @return the list of orders
   */
  List<Order> findByStatus(OrderStatus status);

  /**
   * Finds all orders between dates.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return the list of orders
   */
  List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Finds orders by status and between dates.
   *
   * @param status the order status
   * @param startDate the start date
   * @param endDate the end date
   * @return the list of orders
   */
  List<Order> findByStatusAndDateBetween(
      OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
