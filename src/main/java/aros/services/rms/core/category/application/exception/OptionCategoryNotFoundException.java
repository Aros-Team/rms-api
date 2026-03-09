/* (C) 2026 */
package aros.services.rms.core.category.application.exception;

/** Exception thrown when an option category is not found by its identifier. */
public class OptionCategoryNotFoundException extends RuntimeException {

  public OptionCategoryNotFoundException(Long id) {
    super("Option category not found: " + id);
  }
}
