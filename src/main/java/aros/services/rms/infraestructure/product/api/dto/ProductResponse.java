/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
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
    @Schema(
            description = "Product description",
            example = "Deliciosa hamburguesa con queso cheddar")
        String description,
    @Schema(description = "Product base price") MoneyDto basePrice,
    @Schema(description = "Whether product is active", example = "true") boolean active,
    @Schema(description = "Category ID", example = "1") Long categoryId,
    @Schema(description = "Category name", example = "Hamburguesas") String categoryName,
    @Schema(description = "Preparation area ID", example = "1") Long areaId,
    @Schema(description = "Preparation area name", example = "Cocina") String areaName,
    @Schema(description = "Recipe ingredients required to prepare this product")
        List<RecipeItemResponse> recipe,
    @Schema(description = "Estimated preparation time in minutes", example = "15")
        Integer estimatedPrepMinutes,
    @Schema(description = "People required to prepare the product", example = "2")
        Integer requiredWorkers,
    @Schema(description = "Preparation-time source used for costing", example = "ESTIMATED")
        String prepTimeMode,
    @Schema(
            description = "Selection type (SPECIAL_SELECTION or STANDARD)",
            example = "SPECIAL_SELECTION")
        String selectionType,
    @Schema(
            description = "Signed URL of the primary image (mobile variant)",
            example = "/api/v1/images/local/products/1/mobile.webp")
        String imageUrl,
    @Schema(
            description = "Signed URL of the primary image (tablet variant)",
            example = "/api/v1/images/local/products/1/tablet.webp")
        String imageTabletUrl,
    @Schema(
            description = "Signed URL of the primary image (desktop variant)",
            example = "/api/v1/images/local/products/1/desktop.webp")
        String imageDesktopUrl,
    @Schema(description = "IDs of associated option groups") List<Long> optionGroupIds) {

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
        product.getDescription(),
        product.getBasePrice() != null ? MoneyDto.of(product.getBasePrice()) : null,
        product.isActive(),
        product.getCategory() != null ? product.getCategory().getId() : null,
        product.getCategory() != null ? product.getCategory().getName() : null,
        product.getPreparationAreaId(),
        product.getPreparationAreaName(),
        recipeItems,
        product.getEstimatedPrepMinutes(),
        product.getRequiredWorkers() == null ? 1 : product.getRequiredWorkers(),
        product.getPrepTimeMode() == null ? "ESTIMATED" : product.getPrepTimeMode().name(),
        product.getSelectionType() != null ? product.getSelectionType().name() : "STANDARD",
        null,
        null,
        null,
        product.getOptionGroupIds() != null ? product.getOptionGroupIds() : List.of());
  }

  /**
   * Creates a response from a domain object with the primary image URL resolved separately.
   *
   * @param product the product
   * @param imageUrl the resolved primary image URL (may be null)
   * @return the response DTO
   */
  public static ProductResponse fromDomain(Product product, String imageUrl) {
    return fromDomain(product, imageUrl, null, null);
  }

  /**
   * Creates a response from a domain object with all responsive image variants resolved.
   *
   * @param product the product
   * @param imageUrl mobile variant URL (may be null)
   * @param imageTabletUrl tablet variant URL (may be null)
   * @param imageDesktopUrl desktop variant URL (may be null)
   * @return the response DTO
   */
  public static ProductResponse fromDomain(
      Product product, String imageUrl, String imageTabletUrl, String imageDesktopUrl) {
    ProductResponse base = fromDomain(product);
    if (base == null) {
      return null;
    }
    return new ProductResponse(
        base.id(),
        base.name(),
        base.description(),
        base.basePrice(),
        base.active(),
        base.categoryId(),
        base.categoryName(),
        base.areaId(),
        base.areaName(),
        base.recipe(),
        base.estimatedPrepMinutes(),
        base.requiredWorkers(),
        base.prepTimeMode(),
        base.selectionType(),
        imageUrl,
        imageTabletUrl,
        imageDesktopUrl,
        base.optionGroupIds());
  }
}
