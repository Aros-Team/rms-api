/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Response payload for the weekly opening hours. */
@Schema(description = "Seven weekday opening windows, Monday first")
public record OpeningHoursResponse(@Schema(description = "One entry per weekday") List<Day> days) {

  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Builds the response from domain windows.
   *
   * @param days the domain windows
   * @return the response payload
   */
  public static OpeningHoursResponse fromDomain(List<OpeningHoursDay> days) {
    return new OpeningHoursResponse(days.stream().map(Day::fromDomain).toList());
  }

  /** One weekday window in the response. */
  @Schema(description = "Opening window for one weekday")
  public record Day(
      @Schema(example = "TUESDAY") String dayOfWeek,
      @Schema(example = "false") boolean closed,
      @Schema(example = "08:00", nullable = true) String openTime,
      @Schema(example = "22:00", nullable = true) String closeTime) {

    static Day fromDomain(OpeningHoursDay day) {
      return new Day(
          day.getDayOfWeek().name(),
          day.isClosed(),
          day.getOpenTime() == null ? null : TIME.format(day.getOpenTime()),
          day.getCloseTime() == null ? null : TIME.format(day.getCloseTime()));
    }
  }
}
