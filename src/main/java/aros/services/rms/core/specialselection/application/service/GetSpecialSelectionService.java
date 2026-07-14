package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the use case to retrieve special selection configurations, including those
 * currently available based on their schedule.
 */
public class GetSpecialSelectionService implements GetSpecialSelectionUseCase {

  private final SpecialSelectionRepositoryPort repositoryPort;
  private final SpecialSelectionAvailabilityService availabilityService;

  /**
   * Creates a new get special selection service.
   *
   * @param repositoryPort the special selection repository port
   * @param availabilityService the special selection availability service
   */
  public GetSpecialSelectionService(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionAvailabilityService availabilityService) {
    this.repositoryPort = repositoryPort;
    this.availabilityService = availabilityService;
  }

  @Override
  public Optional<SpecialSelectionConfiguration> findById(Long productId) {
    return repositoryPort.findById(productId);
  }

  @Override
  public List<SpecialSelectionConfiguration> findAll() {
    return repositoryPort.findAll();
  }

  @Override
  public List<SpecialSelectionConfiguration> findAvailableNow() {
    return availabilityService.filterAvailable(
        repositoryPort.findAllActive(), java.time.LocalDateTime.now());
  }
}
