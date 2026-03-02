package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;

public class DeliveryUseCaseImpl implements DeliveryUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final TableRepositoryPort tableRepositoryPort;

    public DeliveryUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort,
            TableRepositoryPort tableRepositoryPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.tableRepositoryPort = tableRepositoryPort;
    }

    @Override
    public Order markAsDelivered(Long id) {
        Order order = orderRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order can only be delivered when in PREPARING status");
        }

        order.setStatus(OrderStatus.DELIVERED);
        Order savedOrder = orderRepositoryPort.save(order);

        if (order.getTable() != null) {
            Table table = order.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableRepositoryPort.save(table);
        }

        return savedOrder;
    }
}