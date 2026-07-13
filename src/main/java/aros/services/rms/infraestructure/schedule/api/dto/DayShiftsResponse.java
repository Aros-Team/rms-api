package aros.services.rms.infraestructure.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response with the shifts for a given day. */
@Schema(description = "Response DTO listing the shifts assigned to a given day of the week")
public record DayShiftsResponse(
    @Schema(description = "Day of the week", example = "MONDAY") String dayOfWeek,
    @Schema(description = "Shifts for this day") List<ShiftDetailResponse> shifts) {}
