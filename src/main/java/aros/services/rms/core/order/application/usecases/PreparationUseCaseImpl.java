package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;

/**
 * Implementación del caso de uso para pasar órdenes a preparación.
 * Toma la orden más antigua de la cola.
 */
public class PreparationUseCaseImpl implements PreparationUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public PreparationUseCaseImpl(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    /**
     * {@inheritDoc}
     * Busca la orden más antigua en QUEUE y cambia a PREPARING.
     */
    @Override
    public Order processNextOrder() {
        Order order = orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE)
                .orElseThrow(() -> new IllegalStateException("No orders in queue"));

        order.setStatus(OrderStatus.PREPARING);
        return orderRepositoryPort.save(order);
    }
}