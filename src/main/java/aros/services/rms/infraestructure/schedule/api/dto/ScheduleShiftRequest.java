package aros.services.rms.infraestructure.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request payload for a schedule shift. */
@Schema(description = "Definition of a single shift within a schedule")
public record ScheduleShiftRequest(
    @Schema(
            description = "Day of the week (MONDAY, TUESDAY, ...)",
            example = "MONDAY",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String dayOfWeek,
    @Schema(
            description = "Shift start time (HH:mm)",
            example = "08:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String startTime,
    @Schema(
            description = "Shift end time (HH:mm)",
            example = "16:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String endTime) {}
