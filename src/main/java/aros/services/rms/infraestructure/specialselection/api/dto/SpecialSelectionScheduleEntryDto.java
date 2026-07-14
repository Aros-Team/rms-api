package aros.services.rms.infraestructure.specialselection.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/** DTO representing a single weekly availability window for a special selection. */
@Schema(description = "Schedule entry for a special selection")
public record SpecialSelectionScheduleEntryDto(
    @Schema(description = "Schedule ID (null for new)", example = "1") Long id,
    @Schema(description = "Day of week", example = "MONDAY") @NotNull String dayOfWeek,
    @Schema(description = "Start time", example = "10:00:00")
        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime startTime,
    @Schema(description = "End time", example = "22:00:00")
        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime endTime) {}
