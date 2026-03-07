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

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.date ASC LIMIT 1")
  Optional<Order> findFirstByStatusOrderByDateAsc(@Param("status") OrderStatus status);

  List<Order> findByStatus(OrderStatus status);

  List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  List<Order> findByStatusAndDateBetween(
      OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
