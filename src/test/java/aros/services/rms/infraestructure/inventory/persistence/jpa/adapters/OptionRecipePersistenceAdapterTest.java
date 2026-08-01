/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.infraestructure.inventory.persistence.jpa.OptionRecipeMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.OptionRecipeRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.OptionRecipeRepository.OptionMaterialCostProjection;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for the option-recipe native cost aggregation adapter. */
@ExtendWith(MockitoExtension.class)
class OptionRecipePersistenceAdapterTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private OptionRecipeRepository optionRecipeRepository;
  @Mock private OptionRecipeMapper optionRecipeMapper;
  @Mock private SupplyVariantRepository supplyVariantRepository;
  @Mock private OptionMaterialCostProjection firstRow;
  @Mock private OptionMaterialCostProjection secondRow;

  private OptionRecipePersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter =
        new OptionRecipePersistenceAdapter(
            optionRecipeRepository, optionRecipeMapper, supplyVariantRepository);
  }

  @Test
  void should_map_batch_native_cost_rows_to_money_by_option_id() {
    List<Long> optionIds = List.of(11L, 12L);
    when(firstRow.getOptionId()).thenReturn(11L);
    when(firstRow.getCost()).thenReturn(new BigDecimal("1250.75"));
    when(secondRow.getOptionId()).thenReturn(12L);
    when(secondRow.getCost()).thenReturn(new BigDecimal("300.00"));
    when(optionRecipeRepository.loadMaterialCostByOptionIds(optionIds))
        .thenReturn(List.of(firstRow, secondRow));

    Map<Long, Money> result = adapter.loadMaterialCostByOptionIds(optionIds);

    assertEquals(2, result.size());
    assertMoney("1250.75", result.get(11L));
    assertMoney("300.00", result.get(12L));
    verify(optionRecipeRepository).loadMaterialCostByOptionIds(optionIds);
  }

  @Test
  void should_map_null_aggregate_to_zero_money() {
    when(firstRow.getOptionId()).thenReturn(11L);
    when(firstRow.getCost()).thenReturn(null);
    when(optionRecipeRepository.loadMaterialCostByOptionIds(List.of(11L)))
        .thenReturn(List.of(firstRow));

    Map<Long, Money> result = adapter.loadMaterialCostByOptionIds(List.of(11L));

    assertMoney("0.00", result.get(11L));
  }

  @Test
  void should_define_one_native_grouped_material_cost_query() throws NoSuchMethodException {
    var method =
        OptionRecipeRepository.class.getMethod("loadMaterialCostByOptionIds", Collection.class);
    var query = method.getAnnotation(org.springframework.data.jpa.repository.Query.class);

    assertTrue(query.nativeQuery());
    assertTrue(query.value().contains("SUM(ore.required_quantity * sv.unit_cost)"));
    assertTrue(query.value().contains("WHERE ore.option_id IN (:optionIds)"));
    assertTrue(query.value().contains("GROUP BY ore.option_id"));
  }

  @Test
  void should_skip_native_query_when_option_ids_are_empty() {
    Map<Long, Money> result = adapter.loadMaterialCostByOptionIds(List.of());

    assertEquals(Map.of(), result);
    verifyNoInteractions(optionRecipeRepository);
  }

  private static void assertMoney(String expectedAmount, Money actual) {
    assertEquals(0, new BigDecimal(expectedAmount).compareTo(actual.amount()));
    assertEquals(COP, actual.currency());
  }
}
