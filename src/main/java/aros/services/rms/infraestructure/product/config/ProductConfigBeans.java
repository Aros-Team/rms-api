/* (C) 2026 */

package aros.services.rms.infraestructure.product.config;

import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.application.service.ProductOptionService;
import aros.services.rms.core.product.application.service.ProductService;
import aros.services.rms.core.product.port.input.ProductOptionUseCase;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of beans for the product module. Registers product and product option use cases.
 */
@Configuration
public class ProductConfigBeans {

  /** Creates bean for product management use case. */
  @Bean
  public ProductUseCase productUseCase(
      ProductRepositoryPort productRepositoryPort,
      AreaRepositoryPort areaRepositoryPort,
      CategoryRepositoryPort categoryRepositoryPort,
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      Logger logger) {
    return new ProductService(
        productRepositoryPort,
        areaRepositoryPort,
        categoryRepositoryPort,
        productRecipeRepositoryPort,
        supplyVariantRepositoryPort,
        inventoryStockUseCase,
        productOptionRepositoryPort,
        logger);
  }

  /** Creates bean for product option management use case. */
  @Bean
  public ProductOptionUseCase productOptionUseCase(
      ProductOptionRepositoryPort productOptionRepositoryPort,
      OptionCategoryRepositoryPort optionCategoryRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      Logger logger) {
    return new ProductOptionService(
        productOptionRepositoryPort,
        optionCategoryRepositoryPort,
        optionRecipeRepositoryPort,
        supplyVariantRepositoryPort,
        logger);
  }
}
