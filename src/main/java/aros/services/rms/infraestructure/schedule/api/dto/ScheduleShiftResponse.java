package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.ScheduleShift;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response with schedule shift details. */
@Schema(description = "Response DTO for a single shift within a schedule")
public record ScheduleShiftResponse(
    @Schema(description = "Shift ID", example = "10") Long id,
    @Schema(description = "Day of the week", example = "MONDAY") String dayOfWeek,
    @Schema(description = "Shift start time (HH:mm)", example = "08:00") String startTime,
    @Schema(description = "Shift end time (HH:mm)", example = "16:00") String endTime) {

  /** Builds a ScheduleShiftResponse from a domain ScheduleShift. */
  public static ScheduleShiftResponse fromDomain(ScheduleShift shift) {
    return new ScheduleShiftResponse(
        shift.id(),
        shift.dayOfWeek().name(),
        shift.startTime().toString(),
        shift.endTime().toString());
  }
}
