/* (C) 2026 */
package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.Product;
import java.util.List;

/**
 * Input port for product management operations. Handles CRUD, logical deletion, and has_options
 * flag for products.
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
}