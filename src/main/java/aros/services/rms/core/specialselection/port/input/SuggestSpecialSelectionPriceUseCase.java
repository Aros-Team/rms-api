package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SuggestedPrice;
import java.math.BigDecimal;

/**
 * Input port for suggesting a price for a special selection based on recipe costs and a target
 * margin.
 */
public interface SuggestSpecialSelectionPriceUseCase {
  /**
   * Computes the suggested price for the given product using the provided margin.
   *
   * @param productId the product identifier
   * @param marginPercent the target margin percentage
   * @return the suggested price with cost breakdown
   */
  SuggestedPrice execute(Long productId, BigDecimal marginPercent);
}
