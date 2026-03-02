package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;

public class PreparationUseCaseImpl implements PreparationUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public PreparationUseCaseImpl(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public Order processNextOrder() {
        Order order = orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE)
                .orElseThrow(() -> new IllegalStateException("No orders in queue"));

        order.setStatus(OrderStatus.PREPARING);
        return orderRepositoryPort.save(order);
    }
}