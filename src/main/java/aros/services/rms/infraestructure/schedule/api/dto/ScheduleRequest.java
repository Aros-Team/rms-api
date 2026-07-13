package aros.services.rms.infraestructure.schedule.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Request payload for creating or updating a schedule. */
@Schema(description = "Request payload to create or update a schedule with its shifts")
public record ScheduleRequest(
    @Schema(description = "Schedule name (unique)", example = "Morning Shift")
        @NotBlank
        @Size(max = 100)
        String name,
    @Schema(description = "Schedule description", example = "Weekday morning shift, 6am-2pm")
        String description,
    @Schema(
            description = "List of shifts for the schedule (at least one)",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        List<ScheduleShiftRequest> shifts) {}
