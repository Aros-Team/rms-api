package aros.services.rms.infraestructure.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response with a single shift detail for the worker's schedule. */
@Schema(description = "Response DTO with details of a single shift in a worker's schedule")
public record ShiftDetailResponse(
    @Schema(description = "Name of the schedule this shift belongs to", example = "Morning Shift")
        String scheduleName,
    @Schema(description = "Shift start time (HH:mm)", example = "08:00") String startTime,
    @Schema(description = "Shift end time (HH:mm)", example = "16:00") String endTime) {}
