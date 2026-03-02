package aros.services.rms.core.order.port.output;

import aros.services.rms.core.order.domain.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
}