package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response with schedule details including shifts. */
@Schema(description = "Response with schedule details including its shifts")
public record ScheduleResponse(
    @Schema(description = "Schedule ID", example = "1") Long id,
    @Schema(description = "Schedule name", example = "Morning Shift") String name,
    @Schema(description = "Schedule description", example = "Weekday morning shift")
        String description,
    @Schema(description = "List of shifts belonging to this schedule")
        List<ScheduleShiftResponse> shifts) {

  /** Builds a ScheduleResponse from a domain Schedule. */
  public static ScheduleResponse fromDomain(Schedule schedule) {
    return new ScheduleResponse(
        schedule.getId().value(),
        schedule.getName(),
        schedule.getDescription(),
        schedule.getShifts().stream().map(ScheduleShiftResponse::fromDomain).toList());
  }
}
