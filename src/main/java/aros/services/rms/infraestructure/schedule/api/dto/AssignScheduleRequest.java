package aros.services.rms.infraestructure.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Request payload for assigning a schedule to a worker. */
@Schema(description = "Request payload to assign an existing schedule to a worker")
public record AssignScheduleRequest(
    @Schema(
            description = "Schedule ID to assign",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long scheduleId) {}
