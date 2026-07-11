package aros.services.rms.infraestructure.schedule.api.dto;

import jakarta.validation.constraints.NotNull;

/** Request payload for assigning a schedule to a worker. */
public record AssignScheduleRequest(@NotNull Long scheduleId) {}
