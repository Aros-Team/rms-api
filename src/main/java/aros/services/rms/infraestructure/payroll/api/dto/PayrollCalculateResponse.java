/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Response DTO with suggested rate calculation. */
@Schema(description = "Response with suggested rate calculation")
public record PayrollCalculateResponse(
    @Schema(description = "User ID", example = "1") Long userId,
    @Schema(description = "Event type", example = "OVERTIME") String eventType,
    @Schema(description = "User's hourly rate", example = "15625") BigDecimal hourlyRate,
    @Schema(description = "Applied multiplier", example = "1.5") BigDecimal multiplier,
    @Schema(description = "Suggested unit rate", example = "23437.50") BigDecimal suggestedUnitRate,
    @Schema(description = "Suggested amount (quantity x unitRate)", example = "58593.75")
        BigDecimal suggestedAmount) {}
