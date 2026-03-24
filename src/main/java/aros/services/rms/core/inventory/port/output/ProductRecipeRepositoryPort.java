/* (C) 2026 */
package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import java.util.List;

/** Output port for product recipe persistence operations. */
public interface ProductRecipeRepositoryPort {

  List<ProductRecipe> saveAll(List<ProductRecipe> recipes);

  List<ProductRecipe> findByProductId(Long productId);

  void deleteByProductId(Long productId);
}
