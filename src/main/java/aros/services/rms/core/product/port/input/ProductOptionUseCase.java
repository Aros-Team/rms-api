/* (C) 2026 */

package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;

/**
 * Input port for product option management. Product options are specific customization choices
 * (e.g., "Medium rare", "Almond milk") linked to an option category.
 */
public interface ProductOptionUseCase {

  /**
   * Creates a new product option.
   *
   * @param productOption the product option data to create
   * @return the created product option with generated ID
   */
  ProductOption create(ProductOption productOption);

  /**
   * Updates an existing product option.
   *
   * @param id the product option identifier
   * @param productOption the product option data with updates
   * @return the updated product option
   */
  ProductOption update(Long id, ProductOption productOption);

  /**
   * Retrieves all product options.
   *
   * @return list of all product options
   */
  List<ProductOption> findAll();

  /**
   * Finds a product option by its identifier.
   *
   * @param id the product option identifier
   * @return the found product option
   */
  ProductOption findById(Long id);

  /**
   * Finds all product options for a specific product.
   *
   * @param productId the product identifier
   * @return list of product options for the product
   */
  List<ProductOption> findByProductId(Long productId);
}
