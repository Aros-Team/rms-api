/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.config;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.inventory.application.service.InventoryMovementService;
import aros.services.rms.core.inventory.application.service.InventoryStockService;
import aros.services.rms.core.inventory.application.service.TransferInventoryService;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of beans for the inventory module.
 *
 * <p>Core use case implementations are registered as named beans (no @Transactional). The
 * transactional wrappers are annotated with @Primary so Spring injects them wherever the use case
 * ports are required.
 */
@Configuration
public class InventoryConfigBeans {

  /**
   * Registers the core inventory stock use case impl as a named bean. It has no @Transactional —
   * transaction boundaries are managed by InventoryStockTransactionalService.
   */
  @Bean("inventoryStockUseCaseImpl")
  public InventoryStockService inventoryStockUseCaseImpl(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort) {
    return new InventoryStockService(
        productRecipeRepositoryPort,
        optionRecipeRepositoryPort,
        inventoryStockRepositoryPort,
        storageLocationRepositoryPort);
  }

  /**
   * Registers the core inventory movement use case impl as a named bean. It has no @Transactional —
   * transaction boundaries are managed by InventoryMovementService.
   */
  @Bean("inventoryMovementUseCaseImpl")
  public InventoryMovementService inventoryMovementUseCaseImpl(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort,
      BusinessMetricsPort metricsPort) {
    return new InventoryMovementService(
        productRecipeRepositoryPort,
        optionRecipeRepositoryPort,
        inventoryStockRepositoryPort,
        inventoryMovementRepositoryPort,
        storageLocationRepositoryPort,
        metricsPort);
  }

  /**
   * Registers the core transfer inventory use case impl as a bean. Transaction boundaries are
   * managed by the controller's @Transactional annotation.
   */
  @Bean
  public TransferInventoryService transferInventoryUseCase(
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort) {
    return new TransferInventoryService(
        supplyVariantRepositoryPort,
        inventoryStockRepositoryPort,
        inventoryMovementRepositoryPort,
        storageLocationRepositoryPort);
  }
}
