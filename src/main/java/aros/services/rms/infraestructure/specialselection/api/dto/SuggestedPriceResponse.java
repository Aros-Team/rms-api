package aros.services.rms.infraestructure.specialselection.api.dto;

import aros.services.rms.core.specialselection.domain.SuggestedPrice;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** Response payload containing the result of a suggested price calculation. */
@Schema(description = "Suggested price calculation result")
public record SuggestedPriceResponse(
    @Schema(description = "Suggested selling price") BigDecimal suggestedPrice,
    @Schema(description = "Total ingredient cost") BigDecimal totalCost,
    @Schema(description = "Applied margin percent") BigDecimal marginPercent,
    @Schema(description = "Cost breakdown items") List<CostBreakdownItem> breakdown) {

  /**
   * Maps a domain suggested price entity to its API response representation.
   *
   * @param price the domain suggested price
   * @return the API response, or null if the input is null
   */
  public static SuggestedPriceResponse fromDomain(SuggestedPrice price) {
    if (price == null) {
      return null;
    }
    return new SuggestedPriceResponse(
        price.getSuggestedPrice(),
        price.getTotalCost(),
        price.getMarginPercent(),
        price.getBreakdown() != null
            ? price.getBreakdown().stream()
                .map(
                    b ->
                        new CostBreakdownItem(
                            b.getOptionId(), b.getProductId(), b.getName(), b.getCost()))
                .collect(Collectors.toList())
            : null);
  }

  /** Individual cost item contributing to the suggested price. */
  @Schema(description = "Individual cost item")
  public record CostBreakdownItem(
      @Schema(description = "Product option ID (null for base recipe)") Long optionId,
      @Schema(description = "Product ID (null for base recipe)") Long productId,
      @Schema(description = "Item display name") String name,
      @Schema(description = "Cost") BigDecimal cost) {}
}
