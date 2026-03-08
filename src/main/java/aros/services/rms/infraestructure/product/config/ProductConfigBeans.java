/* (C) 2026 */
package aros.services.rms.infraestructure.product.config;

import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.product.application.usecases.ProductOptionUseCaseImpl;
import aros.services.rms.core.product.application.usecases.ProductUseCaseImpl;
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
      Logger logger) {
    return new ProductUseCaseImpl(
        productRepositoryPort, areaRepositoryPort, categoryRepositoryPort, logger);
  }

  /** Creates bean for product option management use case. */
  @Bean
  public ProductOptionUseCase productOptionUseCase(
      ProductOptionRepositoryPort productOptionRepositoryPort,
      OptionCategoryRepositoryPort optionCategoryRepositoryPort,
      Logger logger) {
    return new ProductOptionUseCaseImpl(
        productOptionRepositoryPort, optionCategoryRepositoryPort, logger);
  }
}