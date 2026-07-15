package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;

/** Input port for toggling the active flag of a special selection configuration. */
public interface UpdateSpecialSelectionActiveUseCase {
  /**
   * Updates the active flag of the special selection for the given product.
   *
   * @param productId the product identifier
   * @param active the new active value
   * @param changedBy the user performing the update
   * @return the updated configuration
   */
  SpecialSelectionConfiguration execute(Long productId, boolean active, String changedBy);
}
