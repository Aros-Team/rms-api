package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del caso de uso para consultar órdenes.
 * Soporta filtros por estado y rango de fechas.
 */
public class OrderQueryUseCaseImpl implements OrderQueryUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public OrderQueryUseCaseImpl(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    /**
     * {@inheritDoc}
     * Valida que endDate no sea futura y startDate <= endDate.
     */
    @Override
    public List<Order> findOrders(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        boolean hasStatus = status != null;
        boolean hasDates = startDate != null && endDate != null;

        if (hasDates && endDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }

        if (hasDates && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (hasStatus && hasDates) {
            return orderRepositoryPort.findByStatusAndDateBetween(status, startDate, endDate);
        }

        if (hasStatus) {
            return orderRepositoryPort.findByStatus(status);
        }

        if (hasDates) {
            return orderRepositoryPort.findByDateBetween(startDate, endDate);
        }

        return orderRepositoryPort.findAll();
    }
}