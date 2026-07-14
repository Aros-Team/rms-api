package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;

/** Input port for creating a new special selection configuration. */
public interface CreateSpecialSelectionUseCase {
  /**
   * Creates a new special selection configuration and records the initial history entry.
   *
   * @param config the configuration to create
   * @param createdBy the user creating the configuration
   * @return the saved configuration
   */
  SpecialSelectionConfiguration execute(SpecialSelectionConfiguration config, String createdBy);
}
