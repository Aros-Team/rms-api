package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Request DTO to update only the base price of an existing special selection. */
@Schema(description = "Request to update only the base price of a special selection")
public record SpecialSelectionPricePatchRequest(
    @Schema(description = "New base price", example = "15.99") @NotNull Double basePrice) {}
