package aros.services.rms.infraestructure.order.config;

import aros.services.rms.core.order.application.usecases.AddProductToOrderUseCase;
import aros.services.rms.core.order.domain.OrderRepository;
import aros.services.rms.core.order.port.input.AddProductToOrderInput;
import aros.services.rms.core.product.domain.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderUseCaseConfig {

    @Bean
    public AddProductToOrderInput addProductToOrderInput(
            OrderRepository orderRepository, 
            ProductRepository productRepository) {
        
        // Aquí instanciamos nuestra clase pura de Java y se la entregamos a Spring
        return new AddProductToOrderUseCase(orderRepository, productRepository);
    }
}