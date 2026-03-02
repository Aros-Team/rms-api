package aros.services.rms.infraestructure.order.persistence.jpa;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        aros.services.rms.infraestructure.order.persistence.Order orderEntity = orderMapper.toEntity(order);
        aros.services.rms.infraestructure.order.persistence.Order savedEntity = orderRepository.save(orderEntity);
        return orderMapper.toDomain(savedEntity);
    }
}