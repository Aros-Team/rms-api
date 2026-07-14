package aros.services.rms.core.specialselection.port.output;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import java.util.List;
import java.util.Optional;

/** Output port for special selection configuration persistence operations. */
public interface SpecialSelectionRepositoryPort {
  /**
   * Persists a special selection configuration.
   *
   * @param config the configuration to save
   * @return the saved configuration
   */
  SpecialSelectionConfiguration save(SpecialSelectionConfiguration config);

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
   * Retrieves all active special selection configurations.
   *
   * @return list of active configurations
   */
  List<SpecialSelectionConfiguration> findAllActive();

  /**
   * Soft-deletes the configuration associated with the given product identifier.
   *
   * @param productId the product identifier
   */
  void deleteSoft(Long productId);

  /**
   * Checks whether a configuration exists for the given product identifier.
   *
   * @param productId the product identifier
   * @return true if a configuration exists
   */
  boolean existsById(Long productId);
}
