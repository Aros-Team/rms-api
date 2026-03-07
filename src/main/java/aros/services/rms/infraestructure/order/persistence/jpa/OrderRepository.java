package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.infraestructure.order.persistence.Order;
import aros.services.rms.infraestructure.order.persistence.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findFirstByStatusOrderByDateAsc(OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByStatusAndDateBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}