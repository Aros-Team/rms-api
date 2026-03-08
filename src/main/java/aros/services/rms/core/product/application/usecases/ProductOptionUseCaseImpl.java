/* (C) 2026 */
package aros.services.rms.core.product.application.usecases;

import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.product.application.exception.ProductOptionNotFoundException;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.input.ProductOptionUseCase;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import java.util.List;

/**
 * Implementation of product option management use cases. Validates option category existence before
 * creating/updating product options.
 */
public class ProductOptionUseCaseImpl implements ProductOptionUseCase {

  private final ProductOptionRepositoryPort productOptionRepositoryPort;
  private final OptionCategoryRepositoryPort optionCategoryRepositoryPort;
  private final Logger logger;

  public ProductOptionUseCaseImpl(
      ProductOptionRepositoryPort productOptionRepositoryPort,
      OptionCategoryRepositoryPort optionCategoryRepositoryPort,
      Logger logger) {
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.optionCategoryRepositoryPort = optionCategoryRepositoryPort;
    this.logger = logger;
  }

  @Override
  public ProductOption create(ProductOption productOption) {
    validateOptionCategoryExists(productOption.getCategory().getId());

    ProductOption saved = productOptionRepositoryPort.save(productOption);
    logger.info("ProductOption created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Override
  public ProductOption update(Long id, ProductOption productOption) {
    ProductOption existing =
        productOptionRepositoryPort
            .findById(id)
            .orElseThrow(() -> new ProductOptionNotFoundException(id));

    validateOptionCategoryExists(productOption.getCategory().getId());

    existing.setName(productOption.getName());
    existing.setCategory(productOption.getCategory());

    ProductOption saved = productOptionRepositoryPort.save(existing);
    logger.info("ProductOption updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Override
  public List<ProductOption> findAll() {
    return productOptionRepositoryPort.findAll();
  }

  @Override
  public ProductOption findById(Long id) {
    return productOptionRepositoryPort
        .findById(id)
        .orElseThrow(() -> new ProductOptionNotFoundException(id));
  }

  /** Validates that the option category exists. */
  private void validateOptionCategoryExists(Long optionCategoryId) {
    if (optionCategoryId == null || !optionCategoryRepositoryPort.existsById(optionCategoryId)) {
      throw new OptionCategoryNotFoundException(optionCategoryId);
    }
  }
}