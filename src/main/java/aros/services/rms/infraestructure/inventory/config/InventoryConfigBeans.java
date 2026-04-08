/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.config;

import aros.services.rms.core.inventory.application.usecases.InventoryMovementUseCaseImpl;
import aros.services.rms.core.inventory.application.usecases.InventoryStockUseCaseImpl;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration of beans for the inventory module.
 *
 * <p>InventoryMovementUseCaseImpl is registered as a plain bean (no @Transactional). The
 * transactional wrapper is InventoryMovementService, which is annotated with @Primary so Spring
 * injects it wherever InventoryMovementUseCase is required.
 */
@Configuration
public class InventoryConfigBeans {

  /** Creates bean for inventory stock use case. */
  @Bean
  public InventoryStockUseCase inventoryStockUseCase(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort) {
    return new InventoryStockUseCaseImpl(
        productRecipeRepositoryPort,
        optionRecipeRepositoryPort,
        inventoryStockRepositoryPort,
        storageLocationRepositoryPort);
  }

  /**
   * Registers the core inventory movement use case impl as a named bean. It has no @Transactional
   * — transaction boundaries are managed by InventoryMovementService.
   */
  @Bean("inventoryMovementUseCaseImpl")
  public InventoryMovementUseCaseImpl inventoryMovementUseCaseImpl(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort) {
    return new InventoryMovementUseCaseImpl(
        productRecipeRepositoryPort,
        optionRecipeRepositoryPort,
        inventoryStockRepositoryPort,
        inventoryMovementRepositoryPort,
        storageLocationRepositoryPort);
  }
}
