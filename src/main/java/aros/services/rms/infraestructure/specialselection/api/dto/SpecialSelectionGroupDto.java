package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/** DTO representing a product-category group within a special selection configuration. */
@Schema(description = "Product-category group within a special selection")
public record SpecialSelectionGroupDto(
    @Schema(description = "Group ID (null for new groups)", example = "1") Long id,
    @Schema(description = "Product category ID", example = "5") @NotNull Long categoryId,
    @Schema(description = "Display order", example = "1") int displayOrder,
    @Schema(description = "Whether selection is required", example = "true") boolean required,
    @Schema(description = "Minimum selections", example = "1") @Positive int minSelections,
    @Schema(description = "Maximum selections", example = "1") @Positive int maxSelections,
    @Schema(description = "Available product IDs from this category") List<Long> productIds) {}
