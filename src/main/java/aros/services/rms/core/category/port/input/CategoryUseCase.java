/* (C) 2026 */
package aros.services.rms.core.category.port.input;

import aros.services.rms.core.category.domain.Category;
import java.util.List;

/**
 * Input port for product category management operations. Handles CRUD and enable/disable for
 * product categories (e.g., Entradas, Fuertes, Bebidas).
 */
public interface CategoryUseCase {

  /**
   * Creates a new product category.
   *
   * @param category the category to create
   * @return the created category with generated id
   */
  Category create(Category category);

  /**
   * Updates an existing product category.
   *
   * @param id the category id to update
   * @param category the category data to update
   * @return the updated category
   */
  Category update(Long id, Category category);

  /**
   * Retrieves all product categories.
   *
   * @return list of all categories
   */
  List<Category> findAll();

  /**
   * Retrieves a product category by its id.
   *
   * @param id the category id
   * @return the found category
   */
  Category findById(Long id);

  /**
   * Toggles the enabled status of a product category.
   *
   * @param id the category id to toggle
   * @return the updated category with toggled status
   */
  Category toggleEnabled(Long id);
}