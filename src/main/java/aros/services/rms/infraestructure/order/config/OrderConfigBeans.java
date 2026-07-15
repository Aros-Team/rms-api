/* (C) 2026 */

package aros.services.rms.infraestructure.order.config;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
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
import aros.services.rms.core.specialselection.application.service.SpecialSelectionAvailabilityService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionPricingService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionValidator;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration providing beans for the order-related use cases. */
@Configuration
public class OrderConfigBeans {

  /**
   * Provides the take order use case implementation.
   *
   * @return a configured TakeOrderService instance
   */
  @Bean("takeOrderUseCaseImpl")
  public TakeOrderService takeOrderUseCaseImpl(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase,
      BusinessMetricsPort metricsPort,
      SpecialSelectionRepositoryPort specialSelectionRepositoryPort,
      SpecialSelectionValidator specialSelectionValidator,
      SpecialSelectionPricingService specialSelectionPricingService,
      SpecialSelectionAvailabilityService specialSelectionAvailabilityService) {
    return new TakeOrderService(
        orderRepositoryPort,
        tableRepositoryPort,
        productRepositoryPort,
        productOptionRepositoryPort,
        inventoryStockUseCase,
        inventoryMovementUseCase,
        metricsPort,
        specialSelectionRepositoryPort,
        specialSelectionValidator,
        specialSelectionPricingService,
        specialSelectionAvailabilityService);
  }

  /**
   * Provides the update order use case implementation.
   *
   * @return a configured UpdateOrderService instance
   */
  @Bean
  public UpdateOrderUseCase updateOrderUseCase(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase,
      BusinessMetricsPort metricsPort,
      SpecialSelectionRepositoryPort specialSelectionRepositoryPort,
      SpecialSelectionValidator specialSelectionValidator,
      SpecialSelectionPricingService specialSelectionPricingService,
      SpecialSelectionAvailabilityService specialSelectionAvailabilityService) {
    return new UpdateOrderService(
        orderRepositoryPort,
        tableRepositoryPort,
        productRepositoryPort,
        productOptionRepositoryPort,
        inventoryStockUseCase,
        inventoryMovementUseCase,
        metricsPort,
        specialSelectionRepositoryPort,
        specialSelectionValidator,
        specialSelectionPricingService,
        specialSelectionAvailabilityService);
  }

  /**
   * Provides the preparation use case implementation.
   *
   * @return a configured PreparationService instance
   */
  @Bean
  public PreparationUseCase preparationUseCase(
      OrderRepositoryPort orderRepositoryPort, BusinessMetricsPort metricsPort) {
    return new PreparationService(orderRepositoryPort, metricsPort);
  }

  /**
   * Provides the mark as ready use case implementation.
   *
   * @return a configured MarkAsReadyService instance
   */
  @Bean
  public MarkAsReadyUseCase markAsReadyUseCase(
      OrderRepositoryPort orderRepositoryPort, BusinessMetricsPort metricsPort) {
    return new MarkAsReadyService(orderRepositoryPort, metricsPort);
  }

  /**
   * Provides the delivery use case implementation.
   *
   * @return a configured DeliveryService instance
   */
  @Bean
  public DeliveryUseCase deliveryUseCase(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      BusinessMetricsPort metricsPort) {
    return new DeliveryService(orderRepositoryPort, tableRepositoryPort, metricsPort);
  }

  /**
   * Provides the order query use case implementation.
   *
   * @return a configured OrderQueryService instance
   */
  @Bean
  public OrderQueryUseCase orderQueryUseCase(OrderRepositoryPort orderRepositoryPort) {
    return new OrderQueryService(orderRepositoryPort);
  }
}
