/* (C) 2026 */
package aros.services.rms.infraestructure.order.config;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.application.usecases.DeliveryUseCaseImpl;
import aros.services.rms.core.order.application.usecases.MarkAsReadyUseCaseImpl;
import aros.services.rms.core.order.application.usecases.OrderQueryUseCaseImpl;
import aros.services.rms.core.order.application.usecases.PreparationUseCaseImpl;
import aros.services.rms.core.order.application.usecases.TakeOrderUseCaseImpl;
import aros.services.rms.core.order.application.usecases.UpdateOrderUseCaseImpl;
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
  public TakeOrderUseCaseImpl takeOrderUseCaseImpl(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase,
      BusinessMetricsPort metricsPort) {
    return new TakeOrderUseCaseImpl(
        orderRepositoryPort,
        tableRepositoryPort,
        productRepositoryPort,
        productOptionRepositoryPort,
        inventoryStockUseCase,
        inventoryMovementUseCase,
        metricsPort);
  }

  /** Crea bean para actualizar órdenes existentes. */
  @Bean
  public UpdateOrderUseCase updateOrderUseCase(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase,
      BusinessMetricsPort metricsPort) {
    return new UpdateOrderUseCaseImpl(
        orderRepositoryPort,
        tableRepositoryPort,
        productRepositoryPort,
        productOptionRepositoryPort,
        inventoryStockUseCase,
        inventoryMovementUseCase,
        metricsPort);
  }

  /** Crea bean para pasar órdenes de cola a preparación. */
  @Bean
  public PreparationUseCase preparationUseCase(
      OrderRepositoryPort orderRepositoryPort, BusinessMetricsPort metricsPort) {
    return new PreparationUseCaseImpl(orderRepositoryPort, metricsPort);
  }

  /** Crea bean para marcar órdenes como listas (READY). */
  @Bean
  public MarkAsReadyUseCase markAsReadyUseCase(
      OrderRepositoryPort orderRepositoryPort, BusinessMetricsPort metricsPort) {
    return new MarkAsReadyUseCaseImpl(orderRepositoryPort, metricsPort);
  }

  /** Crea bean para entregar órdenes al cliente. */
  @Bean
  public DeliveryUseCase deliveryUseCase(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      BusinessMetricsPort metricsPort) {
    return new DeliveryUseCaseImpl(orderRepositoryPort, tableRepositoryPort, metricsPort);
  }

  /** Crea bean para consultar órdenes. */
  @Bean
  public OrderQueryUseCase orderQueryUseCase(OrderRepositoryPort orderRepositoryPort) {
    return new OrderQueryUseCaseImpl(orderRepositoryPort);
  }
}
