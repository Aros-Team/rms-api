package aros.services.rms.core.order.port.output;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findFirstByStatusOrderByDateAsc(OrderStatus status);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByStatusAndDateBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}