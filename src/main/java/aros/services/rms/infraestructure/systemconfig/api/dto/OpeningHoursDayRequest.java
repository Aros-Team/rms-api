/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** One weekday of the opening-hours request. */
@Schema(description = "Opening window for one weekday")
public record OpeningHoursDayRequest(
    @Schema(description = "Weekday in English", example = "TUESDAY") @NotNull DayOfWeek dayOfWeek,
    @Schema(description = "True when the restaurant is closed that day", example = "false")
        boolean closed,
    @Schema(description = "Opening time (HH:mm)", example = "08:00", nullable = true)
        String openTime,
    @Schema(description = "Closing time (HH:mm)", example = "22:00", nullable = true)
        String closeTime) {

  /**
   * Converts the request row to the domain window.
   *
   * @return the domain window
   */
  public OpeningHoursDay toDomain() {
    return OpeningHoursDay.builder()
        .dayOfWeek(dayOfWeek)
        .closed(closed)
        .openTime(closed ? null : parse(openTime, "openTime"))
        .closeTime(closed ? null : parse(closeTime, "closeTime"))
        .build();
  }

  private LocalTime parse(String value, String field) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalTime.parse(value);
    } catch (DateTimeParseException e) {
      throw new InvalidConfigurationException(
          "Valor inválido de " + field + " para " + dayOfWeek + ": " + value);
    }
  }
}
