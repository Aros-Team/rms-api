/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Request payload for the weekly opening hours. */
@Schema(description = "Seven weekday opening windows")
public record OpeningHoursRequest(
    @Schema(description = "One entry per weekday, Monday to Sunday")
        @NotNull
        @Size(
            min = 7,
            max = 7,
            message = "El horario de atención debe cubrir los siete días de la semana")
        List<@Valid OpeningHoursDayRequest> days) {

  /**
   * Converts the request to domain windows.
   *
   * @return the domain windows
   */
  public List<OpeningHoursDay> toDomain() {
    return days.stream().map(OpeningHoursDayRequest::toDomain).toList();
  }
}
