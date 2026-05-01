/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Response DTO for product data. */
@Schema(description = "Response DTO for product data")
public record ProductResponse(
    @Schema(description = "Product ID", example = "1") Long id,
    @Schema(description = "Product name", example = "Hamburguesa Clásica") String name,
    @Schema(description = "Product base price", example = "12.50") Double basePrice,
    @Schema(description = "Whether product is active", example = "true") boolean active,
    @Schema(description = "Category ID", example = "1") Long categoryId,
    @Schema(description = "Category name", example = "Hamburguesas") String categoryName,
    @Schema(description = "Preparation area ID", example = "1") Long areaId,
    @Schema(description = "Preparation area name", example = "Cocina") String areaName,
    @Schema(description = "Recipe ingredients required to prepare this product")
        List<RecipeItemResponse> recipe) {

  /** Nested DTO representing a single recipe item (supply variant + quantity). */
  @Schema(description = "A single ingredient in the product recipe")
  public record RecipeItemResponse(
      @Schema(description = "Recipe entry ID", example = "1") Long id,
      @Schema(description = "Supply variant ID", example = "3") Long supplyVariantId,
      @Schema(description = "Required quantity", example = "0.250") BigDecimal requiredQuantity) {

    /** Creates a RecipeItemResponse from a ProductRecipe domain object. */
    public static RecipeItemResponse fromDomain(ProductRecipe recipe) {
      return new RecipeItemResponse(
          recipe.getId(), recipe.getSupplyVariantId(), recipe.getRequiredQuantity());
    }
  }

  /**
   * Creates a response from a domain object.
   *
   * @param product the product
   * @return the response DTO
   */
  public static ProductResponse fromDomain(Product product) {
    if (product == null) {
      return null;
    }
    List<RecipeItemResponse> recipeItems =
        product.getRecipe() != null
            ? product.getRecipe().stream()
                .map(RecipeItemResponse::fromDomain)
                .collect(Collectors.toList())
            : Collections.emptyList();

    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getBasePrice(),
        product.isActive(),
        product.getCategory() != null ? product.getCategory().getId() : null,
        product.getCategory() != null ? product.getCategory().getName() : null,
        product.getPreparationAreaId(),
        product.getPreparationAreaName(),
        recipeItems);
  }
}
