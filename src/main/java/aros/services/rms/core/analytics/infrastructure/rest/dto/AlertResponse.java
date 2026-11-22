/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

/** Response DTO for one variance alert. */
@Schema(description = "Variance alert")
public record AlertResponse(
    @Schema(description = "Alert id", example = "17") Long id,
    @Schema(description = "Severity: RED | YELLOW | INFO", example = "RED") String severity,
    @Schema(description = "Rule type", example = "FOOD_COST_DEVIATION") String type,
    @Schema(description = "Human-readable message") String message,
    @Schema(description = "Metric name", example = "food_cost_pct") String metric,
    @Schema(description = "Baseline value") BigDecimal baseline,
    @Schema(description = "Current value") BigDecimal current,
    @Schema(description = "Deviation percentage", example = "9.00") BigDecimal deviationPct,
    @Schema(description = "Period key", example = "2026-11") String period,
    @Schema(description = "Period bucket", example = "monthly") String bucket,
    @Schema(description = "Detection timestamp") Instant detectedAt,
    @Schema(description = "Status: OPEN | READ | DISMISSED") String status) {}
