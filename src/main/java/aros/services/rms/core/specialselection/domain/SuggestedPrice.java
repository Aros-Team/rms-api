package aros.services.rms.core.specialselection.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the suggested price for a special selection together with the cost breakdown that was
 * used to compute it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedPrice {
  private BigDecimal suggestedPrice;
  private BigDecimal totalCost;
  private BigDecimal marginPercent;
  private List<CostBreakdownItem> breakdown;
  private boolean hasUnitCosts;

  /** Item of the cost breakdown contributing to the suggested price. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CostBreakdownItem {
    private Long optionId;
    private Long productId;
    private String name;
    private BigDecimal cost;
  }
}
