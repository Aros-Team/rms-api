/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Request DTO for creating or updating a product.
 *
 * <p>{@code basePrice} accepts zero so the product-creation wizard can persist a draft (needed to
 * attach recipe, options and images) before the sale price is defined in its final step.
 */
@Schema(
    description = "Request DTO for creating or updating a product",
    example =
        "{\"name\": \"Hamburguesa\", \"description\": \"Con queso cheddar\", \"basePrice\": 12.50, "
            + "\"categoryId\": 1, \"areaId\": 1, \"optionIds\": [1, 2, 3]}")
public record ProductRequest(
    @Schema(description = "Product name", example = "Hamburguesa Clásica")
        @NotBlank(message = "El nombre del producto es obligatorio")
        String name,
    @Schema(
            description = "Product description",
            example = "Deliciosa hamburguesa con queso cheddar, tocino y vegetales frescos")
        String description,
    @Schema(description = "Product base price", example = "12.50")
        @NotNull(message = "El precio base es obligatorio")
        @PositiveOrZero(message = "El precio base debe ser cero o positivo")
        Double basePrice,
    @Schema(description = "Category ID the product belongs to", example = "1")
        @NotNull(message = "El ID de categoría es obligatorio")
        Long categoryId,
    @Schema(description = "Preparation area ID", example = "1")
        @NotNull(message = "El ID del área es obligatorio")
        Long areaId,
    @Schema(
            description = "IDs of existing product options to associate with this product",
            example = "[1, 2, 3]")
        List<Long> optionIds,
    @Schema(
            description =
                "Per-option surcharges. Each entry links an optionId to the surcharge applied when"
                    + " the option is associated with this product. Use alongside or instead of"
                    + " optionIds when each option needs its own extraPrice. May co-exist with"
                    + " optionIds (the surcharge from optionExtras overrides the default 0 for"
                    + " that option).",
            example =
                "[{\"optionId\": 4, \"extraPrice\": 2500.00},"
                    + " {\"optionId\": 5, \"extraPrice\": 1500.00}]")
        @Valid
        List<OptionExtrasRequest> optionExtras,
    @Schema(
            description = "Recipe items (supply variants and quantities)",
            example = "[{\"supplyVariantId\": 1, \"requiredQuantity\": 250.0}]")
        @Valid
        List<RecipeItemRequest> recipe,
    @Schema(description = "Estimated preparation time in minutes", example = "15")
        Integer estimatedPrepMinutes) {}
