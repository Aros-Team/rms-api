package aros.services.rms.infraestructure.specialselection.config;

import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.specialselection.application.service.CreateSpecialSelectionService;
import aros.services.rms.core.specialselection.application.service.DeleteSpecialSelectionService;
import aros.services.rms.core.specialselection.application.service.GetSpecialSelectionHistoryService;
import aros.services.rms.core.specialselection.application.service.GetSpecialSelectionService;
import aros.services.rms.core.specialselection.application.service.RevertSpecialSelectionService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionAvailabilityService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionPricingService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionSnapshotService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionValidator;
import aros.services.rms.core.specialselection.application.service.SuggestSpecialSelectionPriceService;
import aros.services.rms.core.specialselection.application.service.UpdateSpecialSelectionPriceService;
import aros.services.rms.core.specialselection.application.service.UpdateSpecialSelectionScheduleService;
import aros.services.rms.core.specialselection.application.service.UpdateSpecialSelectionService;
import aros.services.rms.core.specialselection.port.input.CreateSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.DeleteSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionHistoryUseCase;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.RevertSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.SuggestSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionScheduleUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration providing beans for the special selection use cases and services. */
@Configuration
public class SpecialSelectionConfigBeans {

  /**
   * Provides the special selection validator bean.
   *
   * @return a configured SpecialSelectionValidator instance
   */
  @Bean
  public SpecialSelectionValidator specialSelectionValidator() {
    return new SpecialSelectionValidator();
  }

  /**
   * Provides the special selection availability service bean.
   *
   * @return a configured SpecialSelectionAvailabilityService instance
   */
  @Bean
  public SpecialSelectionAvailabilityService specialSelectionAvailabilityService() {
    return new SpecialSelectionAvailabilityService();
  }

  /**
   * Provides the special selection snapshot service bean.
   *
   * @param productOptionRepositoryPort the product option repository port
   * @return a configured SpecialSelectionSnapshotService instance
   */
  @Bean
  public SpecialSelectionSnapshotService specialSelectionSnapshotService(
      ProductOptionRepositoryPort productOptionRepositoryPort) {
    return new SpecialSelectionSnapshotService(productOptionRepositoryPort);
  }

  /**
   * Provides the special selection pricing service bean.
   *
   * @param optionRecipeRepositoryPort the option recipe repository port
   * @param productRecipeRepositoryPort the product recipe repository port
   * @param supplyVariantRepositoryPort the supply variant repository port
   * @return a configured SpecialSelectionPricingService instance
   */
  @Bean
  public SpecialSelectionPricingService specialSelectionPricingService(
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort) {
    return new SpecialSelectionPricingService(
        optionRecipeRepositoryPort, productRecipeRepositoryPort, supplyVariantRepositoryPort);
  }

  /**
   * Provides the create special selection use case bean.
   *
   * @return a configured CreateSpecialSelectionService instance
   */
  @Bean
  public CreateSpecialSelectionUseCase createSpecialSelectionUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService,
      SpecialSelectionValidator validator) {
    return new CreateSpecialSelectionService(
        repositoryPort, historyRepositoryPort, snapshotService, validator);
  }

  /**
   * Provides the update special selection use case bean.
   *
   * @return a configured UpdateSpecialSelectionService instance
   */
  @Bean
  public UpdateSpecialSelectionUseCase updateSpecialSelectionUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService,
      SpecialSelectionValidator validator) {
    return new UpdateSpecialSelectionService(
        repositoryPort, historyRepositoryPort, snapshotService, validator);
  }

  /**
   * Provides the update special selection price use case bean.
   *
   * @return a configured UpdateSpecialSelectionPriceService instance
   */
  @Bean
  public UpdateSpecialSelectionPriceUseCase updateSpecialSelectionPriceUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService) {
    return new UpdateSpecialSelectionPriceService(
        repositoryPort, historyRepositoryPort, snapshotService);
  }

  /**
   * Provides the update special selection schedule use case bean.
   *
   * @return a configured UpdateSpecialSelectionScheduleService instance
   */
  @Bean
  public UpdateSpecialSelectionScheduleUseCase updateSpecialSelectionScheduleUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService) {
    return new UpdateSpecialSelectionScheduleService(
        repositoryPort, historyRepositoryPort, snapshotService);
  }

  /**
   * Provides the delete special selection use case bean.
   *
   * @return a configured DeleteSpecialSelectionService instance
   */
  @Bean
  public DeleteSpecialSelectionUseCase deleteSpecialSelectionUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService) {
    return new DeleteSpecialSelectionService(
        repositoryPort, historyRepositoryPort, snapshotService);
  }

  /**
   * Provides the get special selection use case bean.
   *
   * @return a configured GetSpecialSelectionService instance
   */
  @Bean
  public GetSpecialSelectionUseCase getSpecialSelectionUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionAvailabilityService availabilityService) {
    return new GetSpecialSelectionService(repositoryPort, availabilityService);
  }

  /**
   * Provides the get special selection history use case bean.
   *
   * @return a configured GetSpecialSelectionHistoryService instance
   */
  @Bean
  public GetSpecialSelectionHistoryUseCase getSpecialSelectionHistoryUseCase(
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort) {
    return new GetSpecialSelectionHistoryService(historyRepositoryPort);
  }

  /**
   * Provides the revert special selection use case bean.
   *
   * @return a configured RevertSpecialSelectionService instance
   */
  @Bean
  public RevertSpecialSelectionUseCase revertSpecialSelectionUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionHistoryRepositoryPort historyRepositoryPort,
      SpecialSelectionSnapshotService snapshotService,
      SpecialSelectionValidator validator) {
    return new RevertSpecialSelectionService(
        repositoryPort, historyRepositoryPort, snapshotService, validator);
  }

  /**
   * Provides the suggest special selection price use case bean.
   *
   * @return a configured SuggestSpecialSelectionPriceService instance
   */
  @Bean
  public SuggestSpecialSelectionPriceUseCase suggestSpecialSelectionPriceUseCase(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionPricingService pricingService) {
    return new SuggestSpecialSelectionPriceService(repositoryPort, pricingService);
  }
}
