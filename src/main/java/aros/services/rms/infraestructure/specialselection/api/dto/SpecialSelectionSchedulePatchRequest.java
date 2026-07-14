package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Request payload to replace the availability schedule of an existing special selection. */
@Schema(description = "Request to update only the schedule of a special selection")
public record SpecialSelectionSchedulePatchRequest(
    @Schema(description = "Schedule entries") @NotEmpty @Valid
        List<SpecialSelectionScheduleEntryDto> schedule) {}
