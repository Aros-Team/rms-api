package aros.services.rms.infraestructure.schedule.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Request payload for creating or updating a schedule. */
public record ScheduleRequest(
    @NotBlank String name, String description, @NotEmpty List<ScheduleShiftRequest> shifts) {}
