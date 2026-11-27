/* (C) 2026 */

package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Input port for product management operations. Handles CRUD, logical deletion, and option
 * associations for products.
 */
public interface ProductUseCase {

  /**
   * Creates a new product linked to an area and category.
   *
   * @param product the product to create
   * @return the created product with generated id
   */
  Product create(Product product);

  /**
   * Updates an existing product.
   *
   * @param id the product id to update
   * @param product the product data to update
   * @return the updated product
   */
  Product update(Long id, Product product);

  /**
   * Retrieves all products.
   *
   * @return list of all products
   */
  List<Product> findAll();

  /**
   * Retrieves a product by its id.
   *
   * @param id the product id
   * @return the found product
   */
  Product findById(Long id);

  /**
   * Performs logical deletion by setting the product as inactive (disabled).
   *
   * @param id the product id to disable
   * @return the disabled product
   */
  Product disable(Long id);

  /**
   * Reactivates a previously disabled product by setting the active flag to true.
   *
   * @param id the product id to enable
   * @return the enabled product
   */
  Product enable(Long id);

  /**
   * Retrieves all available products (active with sufficient stock in Cocina). Products without a
   * recipe are considered available.
   *
   * @return list of available products
   */
  List<Product> findAllAvailable();

  /**
   * Retrieves products filtered by category ids.
   *
   * @param categoryIds list of category ids to filter by
   * @return list of products in the specified categories
   */
  List<Product> findByCategoryIds(List<Long> categoryIds);

  /**
   * Retrieves all active products with pagination.
   *
   * @param pageable pagination parameters
   * @return page of active products
   */
  Page<Product> findAllActive(Pageable pageable);

  /**
   * Retrieves all active products with pagination, optionally including special selection products.
   * When {@code includeSelections} is false (default), only standard products are returned.
   *
   * @param pageable pagination parameters
   * @param includeSelections if true, include special selection products
   * @return page of active products
   */
  Page<Product> findAllActive(Pageable pageable, boolean includeSelections);

  /**
   * Searches products by a partial, case-insensitive term matched against name, description, and
   * category.name. Filters and pagination are applied in the database so the count is correct
   * across the full result set.
   *
   * @param search the search term (required, non-blank)
   * @param categoryIds optional category filter; {@code null} or empty means no category filter
   * @param includeInactive when {@code false}, inactive products are excluded
   * @param includeSelections when {@code false}, only standard selection products are returned
   * @param pageable pagination parameters
   * @return a page of products matching the criteria
   */
  Page<Product> search(
      String search,
      List<Long> categoryIds,
      boolean includeInactive,
      boolean includeSelections,
      Pageable pageable);

  /**
   * Associates a product option with a product (upsert) and records its per-product surcharge.
   *
   * @param productId the product identifier
   * @param optionId the option identifier
   * @param extraPrice the surcharge for this product (nullable -> zero)
   */
  void associateOption(Long productId, Long optionId, BigDecimal extraPrice);

  /**
   * Updates the surcharge of an already-associated product option.
   *
   * @param productId the product identifier
   * @param optionId the option identifier
   * @param extraPrice the new surcharge (nullable -> zero)
   */
  void updateOptionExtraPrice(Long productId, Long optionId, BigDecimal extraPrice);

  /**
   * Removes a single option association from a product.
   *
   * @param productId the product identifier
   * @param optionId the option identifier
   */
  void detachOption(Long productId, Long optionId);

  /**
   * Removes an option group from a product together with every option association that belongs to
   * that group. The group and its options remain in the catalogue.
   *
   * @param productId the product identifier
   * @param optionGroupId the option-group identifier
   */
  void detachOptionGroup(Long productId, Long optionGroupId);
}
