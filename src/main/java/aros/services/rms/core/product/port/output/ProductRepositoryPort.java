/* (C) 2026 */

package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
   * Finds all products by their identifiers.
   *
   * @param ids list of product identifiers
   * @return list of products matching the given identifiers
   */
  List<Product> findAllById(List<Long> ids);

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

  /**
   * Retrieves all active products with pagination.
   *
   * @param pageable pagination parameters
   * @return page of active products
   */
  Page<Product> findAllActive(Pageable pageable);

  /**
   * Retrieves all products using the standard selection type.
   *
   * @return list of standard selection products
   */
  List<Product> findAllStandard();

  /**
   * Retrieves all products using the standard selection type with pagination.
   *
   * @param pageable pagination parameters
   * @return page of standard selection products
   */
  Page<Product> findAllStandard(Pageable pageable);

  /**
   * Finds products with the standard selection type within the given categories.
   *
   * @param categoryIds list of category identifiers
   * @return list of standard selection products in the specified categories
   */
  List<Product> findByCategoryIdsStandard(List<Long> categoryIds);

  /**
   * Searches products by a partial, case-insensitive term matched against the product's name, its
   * description, and its category's name. Combines pagination with optional filters pushed to the
   * database — filtering happens against the full DB before pagination, never on the current page.
   *
   * @param search the search term (required, non-blank)
   * @param categoryIds optional category filter; {@code null} or empty means no category filter
   * @param includeInactive when {@code false}, inactive products are excluded
   * @param includeSelections when {@code false}, only standard selection products are returned
   * @param pageable pagination parameters
   * @return a page of products matching the criteria
   */
  Page<Product> searchByNameOrDescriptionOrCategoryName(
      String search,
      List<Long> categoryIds,
      boolean includeInactive,
      boolean includeSelections,
      Pageable pageable);
}
