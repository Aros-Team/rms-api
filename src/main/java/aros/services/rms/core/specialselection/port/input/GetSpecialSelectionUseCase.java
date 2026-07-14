package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import java.util.List;
import java.util.Optional;

/** Input port for retrieving special selection configurations. */
public interface GetSpecialSelectionUseCase {
  /**
   * Finds a special selection configuration by product identifier.
   *
   * @param productId the product identifier
   * @return optional configuration if found
   */
  Optional<SpecialSelectionConfiguration> findById(Long productId);

  /**
   * Retrieves all special selection configurations.
   *
   * @return list of all configurations
   */
  List<SpecialSelectionConfiguration> findAll();

  /**
   * Retrieves all special selection configurations currently available based on their schedule.
   *
   * @return list of available configurations
   */
  List<SpecialSelectionConfiguration> findAvailableNow();
}
