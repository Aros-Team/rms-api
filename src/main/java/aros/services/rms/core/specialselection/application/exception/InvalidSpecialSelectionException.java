package aros.services.rms.core.specialselection.application.exception;

import java.util.List;

/** Exception raised when a special selection configuration fails validation. */
public class InvalidSpecialSelectionException extends RuntimeException {
  private final List<String> errors;

  /**
   * Creates a new invalid special selection exception.
   *
   * @param errors the validation errors
   */
  public InvalidSpecialSelectionException(List<String> errors) {
    super("Invalid special selection: " + String.join("; ", errors));
    this.errors = errors;
  }

  public List<String> getErrors() {
    return errors;
  }
}
