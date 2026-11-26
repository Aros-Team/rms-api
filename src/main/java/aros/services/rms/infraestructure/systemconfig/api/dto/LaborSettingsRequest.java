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
        @NotNull(message = "El modo de costo de nómina es obligatorio")
        LaborCostMode mode,
    @Schema(description = "Fallback hourly rate", example = "12000.00")
        @NotNull(message = "La tarifa horaria estándar es obligatoria")
        @DecimalMin(value = "0.0", message = "La tarifa horaria estándar no puede ser negativa")
        BigDecimal standardHourlyRate,
    @Schema(description = "Fallback expected hours per month", example = "160")
        @NotNull(message = "Las horas esperadas por defecto son obligatorias")
        @Min(value = 0, message = "Las horas esperadas por defecto no pueden ser negativas")
        @Max(value = 744, message = "Las horas esperadas por defecto no deben superar 744")
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
