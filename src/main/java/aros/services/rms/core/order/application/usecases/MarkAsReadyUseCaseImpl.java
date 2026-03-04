package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;

/**
 * Implementación del caso de uso para marcar órdenes como listas.
 * Permite al cocinero notificar que finalizó la preparación.
 */
public class MarkAsReadyUseCaseImpl implements MarkAsReadyUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public MarkAsReadyUseCaseImpl(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    /**
     * {@inheritDoc}
     * Cambia el estado de PREPARING a READY.
     */
    @Override
    public Order markAsReady(Long id) {
        Order order = orderRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order can only be marked as ready when in PREPARING status");
        }

        order.setStatus(OrderStatus.READY);
        return orderRepositoryPort.save(order);
    }
}