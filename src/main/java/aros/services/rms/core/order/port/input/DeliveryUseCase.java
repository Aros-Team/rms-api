package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.domain.Order;

public interface DeliveryUseCase {
    Order markAsDelivered(Long id);
}