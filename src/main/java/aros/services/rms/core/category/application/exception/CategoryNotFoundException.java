/* (C) 2026 */
package aros.services.rms.core.category.application.exception;

/**
 * Exception thrown when a category is not found by its identifier.
 */
public class CategoryNotFoundException extends RuntimeException {

  public CategoryNotFoundException(Long id) {
    super("Category not found: " + id);
  }
}