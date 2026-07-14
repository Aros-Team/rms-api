package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;

/** Input port for deleting (deactivating) an existing special selection configuration. */
public interface DeleteSpecialSelectionUseCase {
  /**
   * Deactivates the special selection associated with the given product identifier.
   *
   * @param productId the product identifier
   * @param changedBy the user performing the deletion
   * @return the deactivated configuration
   */
  SpecialSelectionConfiguration execute(Long productId, String changedBy);
}
