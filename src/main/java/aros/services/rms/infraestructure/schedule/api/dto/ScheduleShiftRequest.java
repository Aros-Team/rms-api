package aros.services.rms.infraestructure.schedule.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ScheduleShiftRequest(
    @NotBlank String dayOfWeek, @NotBlank String startTime, @NotBlank String endTime) {}
