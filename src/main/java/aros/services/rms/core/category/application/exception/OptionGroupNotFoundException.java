/* (C) 2026 */

package aros.services.rms.core.category.application.exception;

/** Exception thrown when an option group is not found by its identifier. */
public class OptionGroupNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for missing option group.
   *
   * @param id the option group identifier that was not found
   */
  public OptionGroupNotFoundException(Long id) {
    super("Option category not found: " + id);
  }
}
