package aros.services.rms.infraestructure.order.config;

import aros.services.rms.core.order.application.usecases.DeliveryUseCaseImpl;
import aros.services.rms.core.order.application.usecases.OrderQueryUseCaseImpl;
import aros.services.rms.core.order.application.usecases.PreparationUseCaseImpl;
import aros.services.rms.core.order.application.usecases.TakeOrderUseCaseImpl;
import aros.services.rms.core.order.application.usecases.UpdateOrderUseCaseImpl;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfigBeans {

    @Bean
    public TakeOrderUseCaseImpl takeOrderUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort,
            TableRepositoryPort tableRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            ProductOptionRepositoryPort productOptionRepositoryPort
    ) {
        return new TakeOrderUseCaseImpl(
                orderRepositoryPort,
                tableRepositoryPort,
                productRepositoryPort,
                productOptionRepositoryPort
        );
    }

    @Bean
    public UpdateOrderUseCase updateOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            TableRepositoryPort tableRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            ProductOptionRepositoryPort productOptionRepositoryPort
    ) {
        return new UpdateOrderUseCaseImpl(
                orderRepositoryPort,
                tableRepositoryPort,
                productRepositoryPort,
                productOptionRepositoryPort
        );
    }

    @Bean
    public PreparationUseCase preparationUseCase(
            OrderRepositoryPort orderRepositoryPort
    ) {
        return new PreparationUseCaseImpl(orderRepositoryPort);
    }

    @Bean
    public DeliveryUseCase deliveryUseCase(
            OrderRepositoryPort orderRepositoryPort,
            TableRepositoryPort tableRepositoryPort
    ) {
        return new DeliveryUseCaseImpl(orderRepositoryPort, tableRepositoryPort);
    }

    @Bean
    public OrderQueryUseCase orderQueryUseCase(
            OrderRepositoryPort orderRepositoryPort
    ) {
        return new OrderQueryUseCaseImpl(orderRepositoryPort);
    }
}
