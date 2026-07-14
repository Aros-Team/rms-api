package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Request payload used to create or fully replace a special selection configuration. */
@Schema(description = "Request to create or update a special selection configuration")
public record SpecialSelectionRequest(
    @Schema(description = "Product ID", example = "1") @NotNull Long productId,
    @Schema(description = "Display name", example = "Menú ejecutivo") @NotBlank String name,
    @Schema(description = "Description", example = "Sopa + plato fuerte + postre")
        String description,
    @Schema(description = "Base price", example = "12.99") @NotNull Double basePrice,
    @Schema(description = "Base recipe cost enabled", example = "false") boolean baseRecipeEnabled,
    @Schema(description = "Schedule required", example = "true") boolean schedulingRequired,
    @Schema(description = "Option groups") @Valid List<SpecialSelectionGroupDto> groups,
    @Schema(description = "Paid additions") @Valid List<SpecialSelectionAdditionDto> additions,
    @Schema(description = "Clarification questions") @Valid
        List<SpecialSelectionQuestionDto> questions,
    @Schema(description = "Availability schedule") @Valid
        List<SpecialSelectionScheduleEntryDto> schedule) {}
