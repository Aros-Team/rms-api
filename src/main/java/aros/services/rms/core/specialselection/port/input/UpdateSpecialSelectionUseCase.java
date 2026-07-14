package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;

/** Input port for updating an existing special selection configuration. */
public interface UpdateSpecialSelectionUseCase {
  /**
   * Updates the special selection associated with the given product and records the change.
   *
   * @param productId the product identifier
   * @param config the new configuration
   * @param changedBy the user performing the update
   * @return the updated configuration
   */
  SpecialSelectionConfiguration execute(
      Long productId, SpecialSelectionConfiguration config, String changedBy);
}
