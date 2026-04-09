/* (C) 2026 */
package aros.services.rms.infraestructure.category.config;

import aros.services.rms.core.category.application.service.CategoryService;
import aros.services.rms.core.category.application.service.OptionCategoryService;
import aros.services.rms.core.category.port.input.CategoryUseCase;
import aros.services.rms.core.category.port.input.OptionCategoryUseCase;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of beans for the category module. Registers category and option category use cases.
 */
@Configuration
public class CategoryConfigBeans {

  /** Creates bean for category management use case. */
  @Bean
  public CategoryUseCase categoryUseCase(
      CategoryRepositoryPort categoryRepositoryPort, Logger logger) {
    return new CategoryService(categoryRepositoryPort, logger);
  }

  /** Creates bean for option category management use case. */
  @Bean
  public OptionCategoryUseCase optionCategoryUseCase(
      OptionCategoryRepositoryPort optionCategoryRepositoryPort, Logger logger) {
    return new OptionCategoryService(optionCategoryRepositoryPort, logger);
  }
}
