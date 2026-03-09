/* (C) 2026 */
package aros.services.rms.core.product.application.usecases;

import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.product.application.exception.ProductOptionNotFoundException;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.input.ProductOptionUseCase;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of product option management use cases. Validates option category existence before
 * creating/updating product options.
 */
public class ProductOptionUseCaseImpl implements ProductOptionUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ProductOptionUseCaseImpl.class);
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
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public ProductOption create(ProductOption productOption) {
    validateOptionCategoryExists(productOption.getCategory().getId());

    ProductOption saved = productOptionRepositoryPort.save(productOption);
    logger.info("ProductOption created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
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
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<ProductOption> findAll() {
    return productOptionRepositoryPort.findAll();
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public ProductOption findById(Long id) {
    return productOptionRepositoryPort
        .findById(id)
        .orElseThrow(() -> new ProductOptionNotFoundException(id));
  }

  @Recover
  public ProductOption recoverCreate(DataAccessException e, ProductOption productOption) {
    log.warn("BD no disponible - fallback para create(productOption={}): {}", productOption.getName(), e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public ProductOption recoverUpdate(DataAccessException e, Long id, ProductOption productOption) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public List<ProductOption> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public ProductOption recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** Validates that the option category exists. */
  private void validateOptionCategoryExists(Long optionCategoryId) {
    if (optionCategoryId == null || !optionCategoryRepositoryPort.existsById(optionCategoryId)) {
      throw new OptionCategoryNotFoundException(optionCategoryId);
    }
  }
}