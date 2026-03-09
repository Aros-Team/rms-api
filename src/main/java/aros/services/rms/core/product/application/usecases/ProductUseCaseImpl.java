/* (C) 2026 */
package aros.services.rms.core.product.application.usecases;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of product management use cases. Validates area and category existence before
 * creating/updating products. Supports logical deletion via active flag.
 */
public class ProductUseCaseImpl implements ProductUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ProductUseCaseImpl.class);
  private final ProductRepositoryPort productRepositoryPort;
  private final AreaRepositoryPort areaRepositoryPort;
  private final CategoryRepositoryPort categoryRepositoryPort;
  private final Logger logger;

  public ProductUseCaseImpl(
      ProductRepositoryPort productRepositoryPort,
      AreaRepositoryPort areaRepositoryPort,
      CategoryRepositoryPort categoryRepositoryPort,
      Logger logger) {
    this.productRepositoryPort = productRepositoryPort;
    this.areaRepositoryPort = areaRepositoryPort;
    this.categoryRepositoryPort = categoryRepositoryPort;
    this.logger = logger;
  }

  /** {@inheritDoc} Validates that the referenced area and category exist before creating. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Product create(Product product) {
    validateAreaExists(product.getPreparationAreaId());
    validateCategoryExists(product.getCategory().getId());

    product.setActive(true);
    Product saved = productRepositoryPort.save(product);
    logger.info("Product created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /** {@inheritDoc} Validates that the referenced area and category exist before updating. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Product update(Long id, Product product) {
    Product existing =
        productRepositoryPort
            .findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

    validateAreaExists(product.getPreparationAreaId());
    validateCategoryExists(product.getCategory().getId());

    existing.setName(product.getName());
    existing.setBasePrice(product.getBasePrice());
    existing.setHasOptions(product.isHasOptions());
    existing.setCategory(product.getCategory());
    existing.setPreparationAreaId(product.getPreparationAreaId());

    Product saved = productRepositoryPort.save(existing);
    logger.info("Product updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<Product> findAll() {
    return productRepositoryPort.findAll();
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Product findById(Long id) {
    return productRepositoryPort
        .findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
  }

  /** {@inheritDoc} Sets the product active flag to false (logical deletion). */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Product disable(Long id) {
    Product existing =
        productRepositoryPort
            .findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

    existing.setActive(false);

    Product saved = productRepositoryPort.save(existing);
    logger.info("Product disabled: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Recover
  public Product recoverCreate(DataAccessException e, Product product) {
    log.warn("BD no disponible - fallback para create(product={}): {}", product.getName(), e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public Product recoverUpdate(DataAccessException e, Long id, Product product) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public List<Product> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public Product recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public Product recoverDisable(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para disable(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** Validates that the area exists. */
  private void validateAreaExists(Long areaId) {
    if (areaId == null || !areaRepositoryPort.existsById(areaId)) {
      throw new AreaNotFoundException(areaId);
    }
  }

  /** Validates that the category exists. */
  private void validateCategoryExists(Long categoryId) {
    if (categoryId == null || !categoryRepositoryPort.existsById(categoryId)) {
      throw new CategoryNotFoundException(categoryId);
    }
  }
}