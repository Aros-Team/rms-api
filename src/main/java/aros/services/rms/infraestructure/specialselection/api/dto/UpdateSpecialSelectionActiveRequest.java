package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Request payload to update the active status of a special selection combo. */
@Schema(description = "Request to update active status of a combo")
public record UpdateSpecialSelectionActiveRequest(
    @Schema(description = "Active flag", example = "true") @NotNull Boolean active) {}
