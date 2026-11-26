/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.PrepTimeMode;
import aros.services.rms.core.product.domain.ProductPrepTime;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Response DTO for estimated and measured preparation times. */
@Schema(description = "Estimated and measured preparation times for a product")
public record ProductPrepTimeResponse(
    @Schema(description = "Editable estimated minutes") Integer estimatedPrepMinutes,
    @Schema(description = "Measured average minutes", nullable = true) BigDecimal realPrepMinutes,
    @Schema(description = "Valid measured samples") long realPrepSampleCount,
    @Schema(description = "Configured mode") PrepTimeMode prepTimeMode,
    @Schema(description = "Minutes used by the labor cost") BigDecimal usedPrepMinutes,
    @Schema(description = "Source actually used") PrepTimeMode usedSource,
    @Schema(description = "Minimum samples required to use real time") long minimumSamples) {

  /**
   * Maps the domain result to the API shape.
   *
   * @param prepTime the resolved prep time
   * @return the response DTO
   */
  public static ProductPrepTimeResponse fromDomain(ProductPrepTime prepTime) {
    if (prepTime == null) {
      return null;
    }
    return new ProductPrepTimeResponse(
        prepTime.estimatedMinutes(),
        prepTime.realMinutes() == null ? null : BigDecimal.valueOf(prepTime.realMinutes()),
        prepTime.realSampleCount(),
        prepTime.mode(),
        prepTime.usedMinutes() == null ? null : BigDecimal.valueOf(prepTime.usedMinutes()),
        prepTime.usedSource(),
        ProductPrepTime.MIN_REAL_SAMPLES);
  }
}
