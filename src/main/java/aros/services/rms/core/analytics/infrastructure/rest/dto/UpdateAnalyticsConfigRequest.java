/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating analytics operating hours and alert thresholds. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAnalyticsConfigRequest {

  @NotNull
  @Schema(description = "Default restaurant opening time")
  private LocalTime defaultOpen;

  @NotNull
  @Schema(description = "Default restaurant closing time")
  private LocalTime defaultClose;

  @NotNull
  @Schema(description = "Lunch period start time")
  private LocalTime lunchStart;

  @NotNull
  @Schema(description = "Lunch period end time")
  private LocalTime lunchEnd;

  @NotNull
  @Schema(description = "Dinner period start time")
  private LocalTime dinnerStart;

  @NotNull
  @Schema(description = "Dinner period end time")
  private LocalTime dinnerEnd;

  @NotNull
  @DecimalMin("0.0")
  @Digits(integer = 3, fraction = 2)
  @Schema(description = "Non-negative food cost deviation threshold in percentage points")
  private BigDecimal foodCostDeviationPp;

  @NotNull
  @DecimalMin("0.0")
  @Digits(integer = 3, fraction = 2)
  @Schema(description = "Non-negative labor cost deviation threshold in percentage points")
  private BigDecimal laborCostDeviationPp;

  @NotNull
  @DecimalMin("0.0")
  @Digits(integer = 3, fraction = 2)
  @Schema(description = "Non-negative year-over-year sales drop threshold as a percentage")
  private BigDecimal salesDropYoyPct;
}
