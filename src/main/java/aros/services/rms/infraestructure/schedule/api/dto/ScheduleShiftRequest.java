package aros.services.rms.infraestructure.schedule.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Request payload for a schedule shift. */
public record ScheduleShiftRequest(
    @NotBlank String dayOfWeek, @NotBlank String startTime, @NotBlank String endTime) {}
