/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Tests the selection-mode semantics of {@link
 * MenuEngineeringAggregationJpaAdapter#loadAvgOptionCostByProduct(LocalDate, LocalDate)} (Phase D).
 *
 * <ul>
 *   <li>Substitution (SINGLE_CHOICE with {@code replace_supply_category_id}) → {@code optionCost −
 *       defaultSlotCost} (slot cost reused from {@link
 *       ProductOptionRepositoryPort#loadDefaultSlotCostByProductAndCategory()}).
 *   <li>REMOVE → {@code −optionCost}.
 *   <li>EXTRA / MULTI_SELECT / non-replacement SINGLE_CHOICE → {@code +optionCost}.
 *   <li>Order lines without options contribute zero to the average.
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuEngineeringAggregationJpaAdapterSelectionTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private EntityManager entityManager;
  @Mock private Query optionQuery;
  @Mock private Query countQuery;
  @Mock private ProductOptionRepositoryPort productOptionRepositoryPort;

  private MenuEngineeringAggregationJpaAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new MenuEngineeringAggregationJpaAdapter(entityManager, productOptionRepositoryPort);
    when(entityManager.createNativeQuery(anyString())).thenReturn(optionQuery, countQuery);
    when(optionQuery.setParameter(anyString(), any())).thenReturn(optionQuery);
    when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
  }

  private Map<Long, Map<Long, Money>> slotCosts() {
    Map<Long, Map<Long, Money>> slots = new HashMap<>();
    slots.put(1L, Map.of(500L, new Money(new BigDecimal("12.00"), COP)));
    slots.put(2L, Map.of(600L, new Money(new BigDecimal("8.00"), COP)));
    return slots;
  }

  @Test
  void shouldApplySelectionModeContributions_toAvgOptionCost() {
    when(productOptionRepositoryPort.loadDefaultSlotCostByProductAndCategory())
        .thenReturn(slotCosts());

    // [orderId, productId, optionId, selectionType, replaceSupplyCategoryId, optionCost]
    when(optionQuery.getResultList())
        .thenReturn(
            List.of(
                new Object[] {1L, 1L, 1001L, "SINGLE_CHOICE", 500L, new BigDecimal("15.00")},
                new Object[] {2L, 1L, 1002L, "REMOVE", null, new BigDecimal("7.00")},
                new Object[] {3L, 1L, 1003L, "EXTRA", null, new BigDecimal("5.00")},
                new Object[] {3L, 1L, 1004L, "MULTI_SELECT", null, new BigDecimal("6.00")},
                new Object[] {5L, 2L, 2001L, "SINGLE_CHOICE", 600L, new BigDecimal("20.00")},
                new Object[] {6L, 3L, 3001L, "SINGLE_CHOICE", 700L, new BigDecimal("9.00")}));
    when(countQuery.getResultList())
        .thenReturn(List.of(new Object[] {1L, 4L}, new Object[] {2L, 1L}, new Object[] {3L, 1L}));

    Map<Long, Money> result =
        adapter.loadAvgOptionCostByProduct(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1));

    // Producto 1: (15−12) + (−7) + (5+6) = 7,00 sobre 4 líneas de pedido (la 4ª sin opciones) =
    // 1,75
    assertEquals(0, result.get(1L).amount().compareTo(new BigDecimal("1.75")));
    // Producto 2: sustitución 20 − 8 = 12,00 sobre 1 línea
    assertEquals(0, result.get(2L).amount().compareTo(new BigDecimal("12.00")));
    // Producto 3: sustitución sin slot registrado → slot 0 → 9,00 − 0 = 9,00
    assertEquals(0, result.get(3L).amount().compareTo(new BigDecimal("9.00")));

    verify(productOptionRepositoryPort).loadDefaultSlotCostByProductAndCategory();
  }

  @Test
  void shouldDefaultUnknownSelectionTypeToSingleChoiceContribution() {
    when(productOptionRepositoryPort.loadDefaultSlotCostByProductAndCategory())
        .thenReturn(Map.of());

    when(optionQuery.getResultList())
        .thenReturn(
            Collections.singletonList(
                new Object[] {1L, 1L, 1001L, "BOGUS_MODE", null, new BigDecimal("4.00")}));
    when(countQuery.getResultList()).thenReturn(Collections.singletonList(new Object[] {1L, 1L}));

    Map<Long, Money> result =
        adapter.loadAvgOptionCostByProduct(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1));

    // Desconocido → SINGLE_CHOICE sin reemplazo → contribución +optionCost.
    assertEquals(0, result.get(1L).amount().compareTo(new BigDecimal("4.00")));
  }

  @Test
  void shouldBuildSqlWithSelectionMetadataAndDistinctOrderLineDenominator() {
    when(optionQuery.getResultList()).thenReturn(List.of());
    when(countQuery.getResultList()).thenReturn(List.of());

    adapter.loadAvgOptionCostByProduct(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1));

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(entityManager, times(2)).createNativeQuery(sqlCaptor.capture());
    List<String> sqls = sqlCaptor.getAllValues();
    String optionSql = sqls.get(0);
    final String countSql = sqls.get(1);

    assertTrue(optionSql.contains("order_detail_options"));
    assertTrue(optionSql.contains("product_options"));
    assertTrue(optionSql.contains("option_categories"));
    assertTrue(optionSql.contains("oc.selection_type"));
    assertTrue(optionSql.contains("oc.replace_supply_category_id"));
    assertTrue(optionSql.contains("option_recipes"));
    assertTrue(optionSql.contains("supply_variants"));
    assertTrue(optionSql.contains("SUM(oreq.required_quantity * sv.unit_cost)"));
    assertTrue(countSql.contains("COUNT(DISTINCT od.order_id)"));
  }
}
