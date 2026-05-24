package aros.services.rms.infraestructure.schedule.api.dto;

import jakarta.validation.constraints.NotNull;

public record AssignScheduleRequest(@NotNull Long scheduleId) {}
