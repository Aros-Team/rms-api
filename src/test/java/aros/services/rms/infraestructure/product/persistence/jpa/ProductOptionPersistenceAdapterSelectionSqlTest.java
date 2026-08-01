/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests the two Phase D projection methods of {@link ProductOptionPersistenceAdapter}: the
 * substitution-slot base-recipe read ({@link
 * ProductOptionPersistenceAdapter#loadBaseRecipeBySupplyCategory}) and the default-slot cost map
 * ({@link ProductOptionPersistenceAdapter#loadDefaultSlotCostByProductAndCategory}).
 */
@ExtendWith(MockitoExtension.class)
class ProductOptionPersistenceAdapterSelectionSqlTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private ProductOptionRepository productOptionRepository;
  @Mock private ProductMapper productMapper;
  @Mock private EntityManager entityManager;
  @Mock private Query dataQuery;

  private ProductOptionPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter =
        new ProductOptionPersistenceAdapter(productOptionRepository, productMapper, entityManager);
  }

  @Test
  void should_load_base_recipe_by_supply_category_with_three_table_join() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(dataQuery);
    when(dataQuery.setParameter("productId", 1L)).thenReturn(dataQuery);
    when(dataQuery.setParameter("supplyCategoryId", 500L)).thenReturn(dataQuery);
    when(dataQuery.getResultList())
        .thenReturn(
            List.of(
                new Object[] {1L, 10L, new BigDecimal("2.00")},
                new Object[] {1L, 11L, new BigDecimal("1.50")}));

    List<ProductRecipe> recipes = adapter.loadBaseRecipeBySupplyCategory(1L, 500L);

    assertEquals(2, recipes.size());
    assertEquals(10L, recipes.get(0).getSupplyVariantId());
    assertEquals(0, recipes.get(0).getRequiredQuantity().compareTo(new BigDecimal("2.00")));
    assertEquals(11L, recipes.get(1).getSupplyVariantId());

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(entityManager).createNativeQuery(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();
    assertTrue(sql.contains("product_recipes pr"));
    assertTrue(sql.contains("JOIN supply_variants sv ON sv.id = pr.supply_variant_id"));
    assertTrue(sql.contains("JOIN supplies s ON s.id = sv.supply_id"));
    assertTrue(sql.contains("s.supply_category_id = :supplyCategoryId"));
  }

  @Test
  void should_skip_slot_query_when_product_or_category_is_null() {
    List<ProductRecipe> recipes = adapter.loadBaseRecipeBySupplyCategory(null, 500L);

    assertEquals(0, recipes.size());
    verify(entityManager, never()).createNativeQuery(anyString());
  }

  @Test
  void should_load_default_slot_cost_grouped_by_product_and_category() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(dataQuery);
    when(dataQuery.getResultList())
        .thenReturn(
            List.of(
                new Object[] {1L, 500L, new BigDecimal("12.00")},
                new Object[] {1L, 600L, new BigDecimal("5.00")},
                new Object[] {2L, 500L, new BigDecimal("8.00")}));

    Map<Long, Map<Long, Money>> result = adapter.loadDefaultSlotCostByProductAndCategory();

    assertEquals(2, result.size());
    assertEquals(0, result.get(1L).get(500L).amount().compareTo(new BigDecimal("12.00")));
    assertEquals(0, result.get(1L).get(600L).amount().compareTo(new BigDecimal("5.00")));
    assertEquals(0, result.get(2L).get(500L).amount().compareTo(new BigDecimal("8.00")));
    assertEquals(COP, result.get(1L).get(500L).currency());

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(entityManager).createNativeQuery(sqlCaptor.capture());
    String sql = sqlCaptor.getValue();
    assertTrue(sql.contains("GROUP BY pr.product_id, s.supply_category_id"));
    assertTrue(sql.contains("SUM(pr.required_quantity * sv.unit_cost)"));
  }
}
