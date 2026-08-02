/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import aros.services.rms.core.product.domain.ProductOptionCostProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests the option cost-profile native projection. */
@ExtendWith(MockitoExtension.class)
class ProductOptionPersistenceAdapterCostProjectionTest {

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
  void should_read_selection_replacement_and_default_slot_cost_directly() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(dataQuery);
    when(dataQuery.setParameter("productId", 42L)).thenReturn(dataQuery);
    when(dataQuery.getResultList())
        .thenReturn(
            Collections.singletonList(
                new Object[] {
                  7L,
                  "Pollo",
                  3L,
                  "Proteína",
                  new BigDecimal("2.50"),
                  "ADD_ON",
                  9L,
                  new BigDecimal("12.75")
                }));

    List<ProductOptionCostProfile> result = adapter.loadCostProfilesByProductId(42L);

    ProductOptionCostProfile profile = result.getFirst();
    assertEquals("ADD_ON", profile.categorySelectionType());
    assertEquals(9L, profile.replaceSupplyCategoryId());
    assertEquals(0, new BigDecimal("12.75").compareTo(profile.defaultSlotCost().amount()));

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    org.mockito.Mockito.verify(entityManager).createNativeQuery(sqlCaptor.capture());
    String projectionSql = sqlCaptor.getValue();
    assertTrue(projectionSql.contains("COALESCE(oc.selection_type, 'SINGLE_CHOICE')"));
    assertTrue(projectionSql.contains("oc.replace_supply_category_id"));
    assertTrue(projectionSql.contains("SUM(pr.required_quantity * sv.unit_cost)"));
    assertFalse(projectionSql.contains("information_schema"));
  }

  @Test
  void should_default_unknown_selection_type_to_single_choice() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(dataQuery);
    when(dataQuery.setParameter("productId", 42L)).thenReturn(dataQuery);
    when(dataQuery.getResultList())
        .thenReturn(
            Collections.singletonList(
                new Object[] {
                  7L,
                  "Pollo",
                  3L,
                  "Proteína",
                  new BigDecimal("2.50"),
                  "BOGUS_MODE",
                  null,
                  BigDecimal.ZERO
                }));

    List<ProductOptionCostProfile> result = adapter.loadCostProfilesByProductId(42L);

    ProductOptionCostProfile profile = result.getFirst();
    assertEquals("SINGLE_CHOICE", profile.categorySelectionType());
    assertEquals(null, profile.replaceSupplyCategoryId());
    assertEquals(0, BigDecimal.ZERO.compareTo(profile.defaultSlotCost().amount()));
  }
}
