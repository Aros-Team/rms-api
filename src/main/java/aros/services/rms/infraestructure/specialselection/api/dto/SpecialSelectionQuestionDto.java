package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing a clarification question that can be asked when ordering a special selection.
 */
@Schema(description = "Clarification question for a special selection")
public record SpecialSelectionQuestionDto(
    @Schema(description = "Question ID (null for new)", example = "1") Long id,
    @Schema(description = "Question text", example = "¿Alguna alergia?") @NotBlank String question,
    @Schema(description = "Whether answer is required", example = "false") boolean required,
    @Schema(description = "Display order", example = "1") int displayOrder) {}
