/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.infraestructure.inventory.persistence.ProductRecipeEntity;
import aros.services.rms.infraestructure.inventory.persistence.jpa.ProductRecipeMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.ProductRecipeRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import aros.services.rms.infraestructure.product.persistence.Product;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter that connects ProductRecipeRepositoryPort with JPA repository. */
@Component
@RequiredArgsConstructor
public class ProductRecipePersistenceAdapter implements ProductRecipeRepositoryPort {

  private final ProductRecipeRepository productRecipeRepository;
  private final ProductRecipeMapper productRecipeMapper;
  private final SupplyVariantRepository supplyVariantRepository;

  @Override
  @Transactional
  public List<ProductRecipe> saveAll(List<ProductRecipe> recipes) {
    List<ProductRecipeEntity> entities =
        recipes.stream()
            .map(
                recipe -> {
                  ProductRecipeEntity entity = productRecipeMapper.toEntity(recipe);
                  entity.setProduct(Product.builder().id(recipe.getProductId()).build());
                  entity.setSupplyVariant(
                      supplyVariantRepository
                          .findById(recipe.getSupplyVariantId())
                          .orElseThrow(
                              () ->
                                  new IllegalArgumentException(
                                      "SupplyVariant not found: " + recipe.getSupplyVariantId())));
                  return entity;
                })
            .collect(Collectors.toList());

    return productRecipeRepository.saveAll(entities).stream()
        .map(productRecipeMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProductRecipe> findByProductId(Long productId) {
    return productRecipeRepository.findByProductId(productId).stream()
        .map(productRecipeMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteByProductId(Long productId) {
    productRecipeRepository.deleteAll(productRecipeRepository.findByProductId(productId));
  }
}
