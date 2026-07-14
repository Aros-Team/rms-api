package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;

/** Input port for updating the base price of an existing special selection configuration. */
public interface UpdateSpecialSelectionPriceUseCase {
  /**
   * Updates the base price of the special selection associated with the given product.
   *
   * @param productId the product identifier
   * @param basePrice the new base price
   * @param changedBy the user performing the update
   * @return the updated configuration
   */
  SpecialSelectionConfiguration execute(Long productId, Double basePrice, String changedBy);
}
