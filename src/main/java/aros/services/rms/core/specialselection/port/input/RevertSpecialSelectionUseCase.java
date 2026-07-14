package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;

/** Input port for reverting a special selection to a previous history version. */
public interface RevertSpecialSelectionUseCase {
  /**
   * Restores the configuration to the snapshot of the given version and records the reversion.
   *
   * @param productId the product identifier
   * @param version the version to revert to
   * @param changedBy the user performing the reversion
   * @return the restored configuration
   */
  SpecialSelectionConfiguration execute(Long productId, int version, String changedBy);
}
