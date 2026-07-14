package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotFoundException;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SuggestedPrice;
import aros.services.rms.core.specialselection.port.input.SuggestSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.math.BigDecimal;

/**
 * Implementation of the use case to suggest a price for a special selection given a target margin
 * percentage.
 */
public class SuggestSpecialSelectionPriceService implements SuggestSpecialSelectionPriceUseCase {

  private final SpecialSelectionRepositoryPort repositoryPort;
  private final SpecialSelectionPricingService pricingService;

  /**
   * Creates a new suggest special selection price service.
   *
   * @param repositoryPort the special selection repository port
   * @param pricingService the special selection pricing service
   */
  public SuggestSpecialSelectionPriceService(
      SpecialSelectionRepositoryPort repositoryPort,
      SpecialSelectionPricingService pricingService) {
    this.repositoryPort = repositoryPort;
    this.pricingService = pricingService;
  }

  @Override
  public SuggestedPrice execute(Long productId, BigDecimal marginPercent) {
    SpecialSelectionConfiguration config =
        repositoryPort
            .findById(productId)
            .orElseThrow(() -> new SpecialSelectionNotFoundException(productId));

    return pricingService.suggestPrice(config, marginPercent);
  }
}
