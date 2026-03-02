package aros.services.rms.infraestructure.order.config;

import aros.services.rms.core.order.application.usecases.TakeOrderUseCaseImpl;
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
    ){
        return new TakeOrderUseCaseImpl(
                orderRepositoryPort,
                tableRepositoryPort,
                productRepositoryPort,
                productOptionRepositoryPort
        );
    }




}
