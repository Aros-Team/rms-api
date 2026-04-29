/* (C) 2026 */

package aros.services.rms.core.product.application.service;

import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.inventory.application.exception.SupplyVariantNotFoundException;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
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
public class ProductOptionService implements ProductOptionUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ProductOptionService.class);
  private final ProductOptionRepositoryPort productOptionRepositoryPort;
  private final OptionCategoryRepositoryPort optionCategoryRepositoryPort;
  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  private final Logger logger;

  /**
   * Creates a new product option service instance.
   *
   * @param productOptionRepositoryPort the product option repository port
   * @param optionCategoryRepositoryPort the option category repository port
   * @param optionRecipeRepositoryPort the option recipe repository port
   * @param supplyVariantRepositoryPort the supply variant repository port
   * @param logger the logger instance
   */
  public ProductOptionService(
      ProductOptionRepositoryPort productOptionRepositoryPort,
      OptionCategoryRepositoryPort optionCategoryRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      Logger logger) {
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.optionCategoryRepositoryPort = optionCategoryRepositoryPort;
    this.optionRecipeRepositoryPort = optionRecipeRepositoryPort;
    this.supplyVariantRepositoryPort = supplyVariantRepositoryPort;
    this.logger = logger;
  }

  /**
   * Creates a new product option with optional recipe.
   *
   * @param productOption the product option data to create
   * @return the created product option with generated ID
   * @throws OptionCategoryNotFoundException if category does not exist
   * @throws SupplyVariantNotFoundException if any supply variant in recipe does not exist
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public ProductOption create(ProductOption productOption) {
    validateOptionCategoryExists(productOption.getCategory().getId());

    ProductOption saved = productOptionRepositoryPort.save(productOption);

    if (productOption.getRecipe() != null && !productOption.getRecipe().isEmpty()) {
      validateSupplyVariantsExist(productOption.getRecipe());
      List<OptionRecipe> recipesToSave =
          productOption.getRecipe().stream()
              .map(
                  recipe ->
                      OptionRecipe.builder()
                          .optionId(saved.getId())
                          .supplyVariantId(recipe.getSupplyVariantId())
                          .requiredQuantity(recipe.getRequiredQuantity())
                          .build())
              .toList();
      optionRecipeRepositoryPort.saveAll(recipesToSave);
    }

    logger.info("ProductOption created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /**
   * Updates an existing product option.
   *
   * @param id the product option identifier
   * @param productOption the product option data with updates
   * @return the updated product option
   * @throws ProductOptionNotFoundException if option does not exist
   * @throws OptionCategoryNotFoundException if category does not exist
   * @throws SupplyVariantNotFoundException if any supply variant in recipe does not exist
   */
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

    // Delete existing recipes and replace with new ones (or empty if null)
    optionRecipeRepositoryPort.deleteByOptionId(id);

    if (productOption.getRecipe() != null && !productOption.getRecipe().isEmpty()) {
      validateSupplyVariantsExist(productOption.getRecipe());
      List<OptionRecipe> recipesToSave =
          productOption.getRecipe().stream()
              .map(
                  recipe ->
                      OptionRecipe.builder()
                          .optionId(saved.getId())
                          .supplyVariantId(recipe.getSupplyVariantId())
                          .requiredQuantity(recipe.getRequiredQuantity())
                          .build())
              .toList();
      optionRecipeRepositoryPort.saveAll(recipesToSave);
    }

    logger.info("ProductOption updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /**
   * Retrieves all product options.
   *
   * @return list of all product options
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<ProductOption> findAll() {
    return productOptionRepositoryPort.findAll();
  }

  /**
   * Finds a product option by its identifier.
   *
   * @param id the product option identifier
   * @return the found product option
   * @throws ProductOptionNotFoundException if not found
   */
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

  /**
   * Finds all product options for a specific product.
   *
   * @param productId the product identifier
   * @return list of product options for the product
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<ProductOption> findByProductId(Long productId) {
    return productOptionRepositoryPort.findByProductId(productId);
  }

  /**
   * Recovery handler for create operation when database is unavailable.
   *
   * @param e the data access exception
   * @param productOption the product option that was being created
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public ProductOption recoverCreate(DataAccessException e, ProductOption productOption) {
    log.warn(
        "BD no disponible - fallback para create(productOption={}): {}",
        productOption.getName(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for update operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the product option identifier being updated
   * @param productOption the product option data with updates
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public ProductOption recoverUpdate(DataAccessException e, Long id, ProductOption productOption) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findAll operation when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public List<ProductOption> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findById operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the product option identifier being looked up
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public ProductOption recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findByProductId operation when database is unavailable.
   *
   * @param e the data access exception
   * @param productId the product identifier
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public List<ProductOption> recoverFindByProductId(DataAccessException e, Long productId) {
    log.warn(
        "BD no disponible - fallback para findByProductId(productId={}): {}",
        productId,
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** Validates that the option category exists. */
  private void validateOptionCategoryExists(Long optionCategoryId) {
    if (optionCategoryId == null || !optionCategoryRepositoryPort.existsById(optionCategoryId)) {
      throw new OptionCategoryNotFoundException(optionCategoryId);
    }
  }

  /** Validates that all supply variant IDs in the recipe exist. */
  private void validateSupplyVariantsExist(List<OptionRecipe> recipes) {
    for (OptionRecipe recipe : recipes) {
      if (!supplyVariantRepositoryPort.existsById(recipe.getSupplyVariantId())) {
        throw new SupplyVariantNotFoundException(recipe.getSupplyVariantId());
      }
    }
  }
}
