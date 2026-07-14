/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.ProductCost;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Response DTO for product cost calculation. */
@Schema(description = "Response DTO for on-the-fly product production cost calculation")
public record ProductCostResponse(
    @Schema(description = "Product ID", example = "1") Long productId,
    @Schema(description = "Total production cost (material + labor)", example = "25.75")
        BigDecimal totalCost,
    @Schema(description = "Material cost (recipe * unit cost)", example = "15.50")
        BigDecimal materialCost,
    @Schema(description = "Labor cost (avg hourly rate * prep time)", example = "10.25")
        BigDecimal laborCost,
    @Schema(description = "Cost breakdown items") List<CostItem> breakdown) {

  /** A single breakdown entry describing a cost contribution. */
  @Schema(description = "A single cost breakdown entry")
  public record CostItem(
      @Schema(description = "Description of this cost component", example = "Material: variant 3")
          String description,
      @Schema(description = "Amount for this component", example = "15.50") BigDecimal amount,
      @Schema(description = "Type of cost (MATERIAL or LABOR)", example = "MATERIAL")
          String type) {}

  /**
   * Maps a domain {@link ProductCost} to a {@link ProductCostResponse}.
   *
   * @param cost the domain cost
   * @return the response DTO
   */
  public static ProductCostResponse fromDomain(ProductCost cost) {
    if (cost == null) {
      return null;
    }
    List<CostItem> items =
        cost.breakdown() != null
            ? cost.breakdown().stream()
                .map(b -> new CostItem(b.description(), b.amount(), b.type()))
                .collect(Collectors.toList())
            : Collections.emptyList();

    return new ProductCostResponse(
        cost.productId(), cost.totalCost(), cost.materialCost(), cost.laborCost(), items);
  }
}
