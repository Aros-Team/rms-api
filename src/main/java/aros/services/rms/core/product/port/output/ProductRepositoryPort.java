/* (C) 2026 */

package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.Product;
import java.util.List;
import java.util.Optional;

/** Output port for product persistence operations. */
public interface ProductRepositoryPort {
  /**
   * Saves a product to the repository.
   *
   * @param product the product to save
   * @return the saved product with generated ID
   */
  Product save(Product product);

  /**
   * Finds a product by its identifier.
   *
   * @param id the product identifier
   * @return Optional containing the product if found
   */
  Optional<Product> findById(Long id);

  /**
   * Retrieves all products.
   *
   * @return list of all products
   */
  List<Product> findAll();

  /**
   * Finds all products for specific categories.
   *
   * @param categoryIds list of category identifiers
   * @return list of products in the specified categories
   */
  List<Product> findByCategoryIds(List<Long> categoryIds);

  /**
   * Checks if a product exists by its identifier.
   *
   * @param id the product identifier to check
   * @return true if product exists
   */
  boolean existsById(Long id);
}
