/* (C) 2026 */
package aros.services.rms.infraestructure.order.config;

import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.application.service.DeliveryService;
import aros.services.rms.core.order.application.service.MarkAsReadyService;
import aros.services.rms.core.order.application.service.OrderQueryService;
import aros.services.rms.core.order.application.service.PreparationService;
import aros.services.rms.core.order.application.service.TakeOrderService;
import aros.services.rms.core.order.application.service.UpdateOrderService;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans para el módulo de órdenes. Registra todos los casos de uso relacionados
 * con gestión de órdenes.
 */
@Configuration
public class OrderConfigBeans {

  /** Crea bean para tomar nuevas órdenes. */
  @Bean
  public TakeOrderService takeOrderUseCaseImpl(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase) {
    return new TakeOrderService(
        orderRepositoryPort,
        tableRepositoryPort,
        productRepositoryPort,
        productOptionRepositoryPort,
        inventoryStockUseCase,
        inventoryMovementUseCase);
  }

  /** Crea bean para actualizar órdenes existentes. */
  @Bean
  public UpdateOrderUseCase updateOrderUseCase(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase) {
    return new UpdateOrderService(
        orderRepositoryPort,
        tableRepositoryPort,
        productRepositoryPort,
        productOptionRepositoryPort,
        inventoryStockUseCase,
        inventoryMovementUseCase);
  }

  /** Crea bean para pasar órdenes de cola a preparación. */
  @Bean
  public PreparationUseCase preparationUseCase(OrderRepositoryPort orderRepositoryPort) {
    return new PreparationService(orderRepositoryPort);
  }

  /** Crea bean para marcar órdenes como listas (READY). */
  @Bean
  public MarkAsReadyUseCase markAsReadyUseCase(OrderRepositoryPort orderRepositoryPort) {
    return new MarkAsReadyService(orderRepositoryPort);
  }

  /** Crea bean para entregar órdenes al cliente. */
  @Bean
  public DeliveryUseCase deliveryUseCase(
      OrderRepositoryPort orderRepositoryPort, TableRepositoryPort tableRepositoryPort) {
    return new DeliveryService(orderRepositoryPort, tableRepositoryPort);
  }

  /** Crea bean para consultar órdenes. */
  @Bean
  public OrderQueryUseCase orderQueryUseCase(OrderRepositoryPort orderRepositoryPort) {
    return new OrderQueryService(orderRepositoryPort);
  }
}
