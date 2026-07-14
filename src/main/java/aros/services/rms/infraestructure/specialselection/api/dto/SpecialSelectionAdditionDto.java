package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO representing a paid addition that can be added on top of a special selection. */
@Schema(description = "Paid addition (extra) for a special selection")
public record SpecialSelectionAdditionDto(
    @Schema(description = "Addition ID (null for new)", example = "1") Long id,
    @Schema(description = "Product option ID for inventory deduction", example = "5") @NotNull
        Long optionId,
    @Schema(description = "Display name", example = "Extra queso") @NotBlank String name,
    @Schema(description = "Extra price", example = "2.50") double extraPrice,
    @Schema(description = "Display order", example = "1") int displayOrder) {}
