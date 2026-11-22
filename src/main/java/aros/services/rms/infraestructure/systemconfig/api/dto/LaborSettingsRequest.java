/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.LaborCostMode;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request payload for labor costing settings. */
@Schema(description = "Labor costing configuration")
public record LaborSettingsRequest(
    @Schema(description = "Costing strategy", example = "AUTO")
        @NotNull(message = "Labor cost mode is required")
        LaborCostMode mode,
    @Schema(description = "Fallback hourly rate", example = "12000.00")
        @NotNull(message = "Standard hourly rate is required")
        @DecimalMin(value = "0.0", message = "Standard hourly rate must not be negative")
        BigDecimal standardHourlyRate,
    @Schema(description = "Fallback expected hours per month", example = "160")
        @NotNull(message = "Default expected hours are required")
        @Min(value = 0, message = "Default expected hours must not be negative")
        @Max(value = 744, message = "Default expected hours must not exceed 744")
        Integer defaultExpectedHoursPerMonth) {

  /**
   * Builds the response payload from domain settings.
   *
   * @param settings the domain settings
   * @return the response payload
   */
  public static LaborSettingsResponse toResponse(
      aros.services.rms.core.systemconfig.domain.LaborSettings settings) {
    return new LaborSettingsResponse(
        settings.mode(),
        MoneyDto.of(settings.standardHourlyRate()),
        settings.defaultExpectedHoursPerMonth());
  }
}
