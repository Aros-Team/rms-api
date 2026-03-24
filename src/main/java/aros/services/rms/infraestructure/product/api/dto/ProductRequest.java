/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(
    description = "Request DTO for creating or updating a product",
    example =
        """
        {
          "name": "Hamburguesa Clásica",
          "basePrice": 12.50,
          "hasOptions": true,
          "categoryId": 1,
          "areaId": 1,
          "recipe": [
            {
              "supplyVariantId": 1,
              "requiredQuantity": 250.0
            }
          ]
        }
        """)
public record ProductRequest(
    @Schema(description = "Product name", example = "Hamburguesa Clásica")
        @NotBlank(message = "Product name is required") String name,
    @Schema(description = "Product base price", example = "12.50")
        @NotNull(message = "Base price is required") @Positive(message = "Base price must be positive") Double basePrice,
    @Schema(description = "Whether product supports customization options", example = "true")
        @NotNull(message = "hasOptions flag is required") Boolean hasOptions,
    @Schema(description = "Category ID the product belongs to", example = "1")
        @NotNull(message = "Category ID is required") Long categoryId,
    @Schema(description = "Preparation area ID", example = "1")
        @NotNull(message = "Area ID is required") Long areaId,
    @Schema(
            description = "Recipe items (supply variants and quantities)",
            example = "[{\"supplyVariantId\": 1, \"requiredQuantity\": 250.0}]")
        @Valid List<RecipeItemRequest> recipe) {}
