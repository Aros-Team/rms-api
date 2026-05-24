package aros.services.rms.infraestructure.schedule.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ScheduleRequest(
    @NotBlank String name, String description, @NotEmpty List<ScheduleShiftRequest> shifts) {}
