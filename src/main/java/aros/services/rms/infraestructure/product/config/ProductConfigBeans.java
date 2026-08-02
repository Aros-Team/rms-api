/* (C) 2026 */

package aros.services.rms.infraestructure.product.config;

import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.category.port.output.OptionGroupRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.application.service.CalculateProductCostService;
import aros.services.rms.core.product.application.service.GetProductCostBreakdownService;
import aros.services.rms.core.product.application.service.ProductOptionService;
import aros.services.rms.core.product.application.service.ProductService;
import aros.services.rms.core.product.port.input.CalculateProductCostUseCase;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.core.product.port.input.ProductOptionUseCase;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import org.springframework.context.ApplicationEventPublisher;
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
      ApplicationEventPublisher eventPublisher,
      Logger logger) {
    return new ProductService(
        productRepositoryPort,
        areaRepositoryPort,
        categoryRepositoryPort,
        productRecipeRepositoryPort,
        supplyVariantRepositoryPort,
        inventoryStockUseCase,
        productOptionRepositoryPort,
        eventPublisher,
        logger);
  }

  /** Creates bean for product option management use case. */
  @Bean
  public ProductOptionUseCase productOptionUseCase(
      ProductOptionRepositoryPort productOptionRepositoryPort,
      OptionGroupRepositoryPort optionGroupRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      Logger logger) {
    return new ProductOptionService(
        productOptionRepositoryPort,
        optionGroupRepositoryPort,
        optionRecipeRepositoryPort,
        supplyVariantRepositoryPort,
        logger);
  }

  /** Creates bean for the product option-cost projection use case. */
  @Bean
  public GetProductCostBreakdownUseCase getProductCostBreakdownUseCase(
      ProductRepositoryPort productRepositoryPort,
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort) {
    return new GetProductCostBreakdownService(
        productRepositoryPort,
        productRecipeRepositoryPort,
        supplyVariantRepositoryPort,
        productOptionRepositoryPort,
        optionRecipeRepositoryPort);
  }

  /** Creates bean for on-the-fly product cost calculation use case. */
  @Bean
  public CalculateProductCostUseCase calculateProductCostUseCase(
      ProductRepositoryPort productRepositoryPort,
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      Logger logger) {
    return new CalculateProductCostService(
        productRepositoryPort,
        productRecipeRepositoryPort,
        supplyVariantRepositoryPort,
        userRepositoryPort,
        logger);
  }
}
