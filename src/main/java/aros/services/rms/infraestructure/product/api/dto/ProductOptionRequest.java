/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Request DTO for product option. */
@Schema(
    description = "Request DTO para crear o actualizar una opción de producto",
    example =
        "{\"name\": \"Grande (1.5L)\", \"optionCategoryId\": 1, "
            + "\"recipe\": [{\"supplyVariantId\": 1, \"requiredQuantity\": 500.0}]}")
public record ProductOptionRequest(
    @Schema(description = "Nombre de la opción", example = "Grande (1.5L)")
        @NotBlank(message = "Option name is required")
        String name,
    @Schema(description = "ID de la categoría de opción a la que pertenece", example = "1")
        @NotNull(message = "Option category ID is required")
        Long optionCategoryId,
    @Schema(
            description = "Recipe items (supply variants and quantities)",
            example = "[{\"supplyVariantId\": 1, \"requiredQuantity\": 500.0}]")
        @Valid
        List<RecipeItemRequest> recipe) {}
