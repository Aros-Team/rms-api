/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for analytics operating hours and alert threshold configuration. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsConfigResponse {

  @Schema(description = "Singleton analytics configuration ID")
  private Integer id;

  @Schema(description = "Default restaurant opening time")
  private LocalTime defaultOpen;

  @Schema(description = "Default restaurant closing time")
  private LocalTime defaultClose;

  @Schema(description = "Lunch period start time")
  private LocalTime lunchStart;

  @Schema(description = "Lunch period end time")
  private LocalTime lunchEnd;

  @Schema(description = "Dinner period start time")
  private LocalTime dinnerStart;

  @Schema(description = "Dinner period end time")
  private LocalTime dinnerEnd;

  @Schema(description = "Food cost deviation threshold in percentage points")
  private BigDecimal foodCostDeviationPp;

  @Schema(description = "Labor cost deviation threshold in percentage points")
  private BigDecimal laborCostDeviationPp;

  @Schema(description = "Year-over-year sales drop threshold as a percentage")
  private BigDecimal salesDropYoyPct;

  @Schema(description = "Timestamp of the latest configuration update")
  private LocalDateTime updatedAt;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "ID of the user who last updated the configuration", nullable = true)
  private Long updatedBy;
}
