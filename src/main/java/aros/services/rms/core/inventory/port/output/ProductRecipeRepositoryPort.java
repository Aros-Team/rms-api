/* (C) 2026 */

package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import java.util.List;

/** Output port for product recipe persistence operations. */
public interface ProductRecipeRepositoryPort {

  /**
   * Saves multiple product recipes.
   *
   * @param recipes the recipes to save
   * @return saved recipes
   */
  List<ProductRecipe> saveAll(List<ProductRecipe> recipes);

  /**
   * Finds recipes by product ID.
   *
   * @param productId the product ID
   * @return list of recipes
   */
  List<ProductRecipe> findByProductId(Long productId);

  /**
   * Deletes recipes by product ID.
   *
   * @param productId the product ID
   */
  void deleteByProductId(Long productId);
}
