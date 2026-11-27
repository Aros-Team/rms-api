/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.inventory.domain.OptionRecipe;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/** Response DTO for one option-recipe ingredient line. */
@Schema(description = "Ingredient line of a product option recipe")
public record RecipeItemResponse(
    @Schema(description = "Supply variant ID", example = "7") Long supplyVariantId,
    @Schema(description = "Required quantity", example = "0.150") BigDecimal requiredQuantity) {

  /**
   * Maps a domain option-recipe line to its response shape.
   *
   * @param recipe the domain line
   * @return the response line, or null when the input is null
   */
  public static RecipeItemResponse fromDomain(OptionRecipe recipe) {
    if (recipe == null) {
      return null;
    }
    return new RecipeItemResponse(recipe.getSupplyVariantId(), recipe.getRequiredQuantity());
  }

  /**
   * Maps a list of option-recipe lines.
   *
   * @param recipes the domain lines
   * @return the response lines, or an empty list when the input is null
   */
  public static List<RecipeItemResponse> fromDomain(List<OptionRecipe> recipes) {
    if (recipes == null || recipes.isEmpty()) {
      return List.of();
    }
    return recipes.stream().map(RecipeItemResponse::fromDomain).toList();
  }
}
