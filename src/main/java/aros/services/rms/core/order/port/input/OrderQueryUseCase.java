package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderQueryUseCase {
    List<Order> findOrders(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}