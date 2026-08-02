/* (C) 2026 */

package aros.services.rms.core.category.application.exception;

/**
 * Thrown when an attempt is made to create or update an {@link
 * aros.services.rms.core.category.domain.OptionGroup} without an associated product. Enforces the
 * business rule that an OptionGroup must be attached to at least one product.
 *
 * <p>Maps to HTTP 400 via {@link
 * aros.services.rms.infraestructure.common.exception.GlobalExceptionHandler}.
 */
public class OptionGroupRequiresProductException extends RuntimeException {

  /** Creates a new exception for an OptionGroup that was offered without product associations. */
  public OptionGroupRequiresProductException() {
    super("An option group must be associated with at least one product");
  }
}
