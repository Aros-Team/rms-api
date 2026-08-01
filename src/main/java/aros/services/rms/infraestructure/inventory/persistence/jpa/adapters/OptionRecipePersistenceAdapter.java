/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.infraestructure.inventory.persistence.OptionRecipeEntity;
import aros.services.rms.infraestructure.inventory.persistence.jpa.OptionRecipeMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.OptionRecipeRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.OptionRecipeRepository.OptionMaterialCostProjection;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import aros.services.rms.infraestructure.product.persistence.ProductOption;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter that connects OptionRecipeRepositoryPort with JPA repository. */
@Component
@RequiredArgsConstructor
public class OptionRecipePersistenceAdapter implements OptionRecipeRepositoryPort {

  private final OptionRecipeRepository optionRecipeRepository;
  private final OptionRecipeMapper optionRecipeMapper;
  private final SupplyVariantRepository supplyVariantRepository;

  @Override
  @Transactional
  public List<OptionRecipe> saveAll(List<OptionRecipe> recipes) {
    List<OptionRecipeEntity> entities =
        recipes.stream()
            .map(
                recipe -> {
                  OptionRecipeEntity entity = optionRecipeMapper.toEntity(recipe);
                  entity.setOption(ProductOption.builder().id(recipe.getOptionId()).build());
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

    return optionRecipeRepository.saveAll(entities).stream()
        .map(optionRecipeMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<OptionRecipe> findByOptionId(Long optionId) {
    return optionRecipeRepository.findByOptionIdIn(List.of(optionId)).stream()
        .map(optionRecipeMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<OptionRecipe> findByOptionIdIn(List<Long> optionIds) {
    return optionRecipeRepository.findByOptionIdIn(optionIds).stream()
        .map(optionRecipeMapper::toDomain)
        .collect(Collectors.toList());
  }

  /** {@inheritDoc} */
  @Override
  public Map<Long, Money> loadMaterialCostByOptionIds(Collection<Long> optionIds) {
    if (optionIds == null || optionIds.isEmpty()) {
      return Map.of();
    }

    Map<Long, Money> costs = new LinkedHashMap<>();
    for (OptionMaterialCostProjection row :
        optionRecipeRepository.loadMaterialCostByOptionIds(optionIds)) {
      BigDecimal cost = row.getCost() == null ? BigDecimal.ZERO : row.getCost();
      costs.put(row.getOptionId(), new Money(cost, Currency.getInstance("COP")));
    }
    return Map.copyOf(costs);
  }

  @Override
  @Transactional
  public void deleteByOptionId(Long optionId) {
    optionRecipeRepository.deleteAll(optionRecipeRepository.findByOptionIdIn(List.of(optionId)));
  }
}
