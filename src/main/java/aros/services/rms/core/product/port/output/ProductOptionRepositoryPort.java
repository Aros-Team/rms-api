/* (C) 2026 */

package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;
import java.util.Optional;

/** Output port for product option persistence operations. */
public interface ProductOptionRepositoryPort {
  /**
   * Saves a product option to the repository.
   *
   * @param productOption the product option to save
   * @return the saved product option with generated ID
   */
  ProductOption save(ProductOption productOption);

  /**
   * Finds a product option by its identifier.
   *
   * @param id the product option identifier
   * @return Optional containing the product option if found
   */
  Optional<ProductOption> findById(Long id);

  /**
   * Finds multiple product options by their identifiers.
   *
   * @param ids list of product option identifiers to find
   * @return list of found product options
   */
  List<ProductOption> findAllById(List<Long> ids);

  /**
   * Retrieves all product options.
   *
   * @return list of all product options
   */
  List<ProductOption> findAll();

  /**
   * Finds all product options for a specific product.
   *
   * @param productId the product identifier
   * @return list of product options for the product
   */
  List<ProductOption> findByProductId(Long productId);

  /**
   * Associates multiple options to a product.
   *
   * @param productId the product identifier
   * @param optionIds list of option identifiers to associate
   */
  void associateOptionsToProduct(Long productId, List<Long> optionIds);

  /**
   * Removes all options from a product.
   *
   * @param productId the product identifier
   */
  void removeAllOptionsFromProduct(Long productId);

  /**
   * Checks if an option is associated with a specific product.
   *
   * @param productId the product identifier
   * @param optionId the option identifier
   * @return true if the option is associated with the product
   */
  boolean isOptionAssociatedWithProduct(Long productId, Long optionId);
}
