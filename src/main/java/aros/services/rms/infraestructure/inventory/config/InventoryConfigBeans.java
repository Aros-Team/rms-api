/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.config;

import aros.services.rms.core.inventory.application.usecases.InventoryMovementUseCaseImpl;
import aros.services.rms.core.inventory.application.usecases.InventoryStockUseCaseImpl;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of beans for the inventory module. Registers inventory stock and movement use
 * cases.
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

  /** Creates bean for inventory movement use case. */
  @Bean
  public InventoryMovementUseCase inventoryMovementUseCase(
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
