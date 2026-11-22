/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.LaborCostMode;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response payload for labor costing settings. */
@Schema(description = "Labor costing configuration")
public record LaborSettingsResponse(
    @Schema(description = "Costing strategy", example = "AUTO") LaborCostMode mode,
    @Schema(description = "Fallback hourly rate") MoneyDto standardHourlyRate,
    @Schema(description = "Fallback expected hours per month", example = "160")
        int defaultExpectedHoursPerMonth) {}
