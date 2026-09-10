/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link OptionGroupPersistenceAdapter}.
 *
 * <p><b>Feature:</b> Option group selection projection
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should read selection type directly when column exists
 *   <li>should default unknown selection type to single choice
 *   <li>should default null selection type to single choice
 *   <li>should skip projection queries when category ids are empty
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks service-layer dependencies via Mockito
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class OptionGroupPersistenceAdapterSelectionProjectionTest {

  @Mock private OptionGroupRepository optionGroupRepository;
  @Mock private ProductOptionGroupJpaRepository productOptionGroupRepository;
  @Mock private CategoryMapper categoryMapper;
  @Mock private EntityManager entityManager;
  @Mock private Query dataQuery;

  private OptionGroupPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter =
        new OptionGroupPersistenceAdapter(
            optionGroupRepository, productOptionGroupRepository, categoryMapper, entityManager);
  }

  @Test
  void should_read_selection_type_directly_when_column_exists() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(dataQuery);
    when(dataQuery.setParameter("ids", List.of(1L))).thenReturn(dataQuery);
    when(dataQuery.getResultList())
        .thenReturn(Collections.singletonList(new Object[] {1L, "ADD_ON"}));

    Map<Long, String> result = adapter.loadSelectionTypesByIds(List.of(1L));

    assertEquals(Map.of(1L, "ADD_ON"), result);
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    org.mockito.Mockito.verify(entityManager).createNativeQuery(sqlCaptor.capture());
    String projectionSql = sqlCaptor.getValue();
    assertTrue(projectionSql.contains("COALESCE(selection_type, 'SINGLE_CHOICE')"));
    assertTrue(projectionSql.contains("FROM option_group"));
    assertFalse(projectionSql.contains("information_schema"));
  }

  @Test
  void should_default_unknown_selection_type_to_single_choice() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(dataQuery);
    when(dataQuery.setParameter("ids", List.of(7L))).thenReturn(dataQuery);
    when(dataQuery.getResultList())
        .thenReturn(Collections.singletonList(new Object[] {7L, "NOT_A_VALID_VALUE"}));

    Map<Long, String> result = adapter.loadSelectionTypesByIds(List.of(7L));

    assertEquals(Map.of(7L, "SINGLE_CHOICE"), result);
  }

  @Test
  void should_default_null_selection_type_to_single_choice() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(dataQuery);
    when(dataQuery.setParameter("ids", List.of(8L))).thenReturn(dataQuery);
    when(dataQuery.getResultList()).thenReturn(Collections.singletonList(new Object[] {8L, null}));

    Map<Long, String> result = adapter.loadSelectionTypesByIds(List.of(8L));

    assertEquals(Map.of(8L, "SINGLE_CHOICE"), result);
  }

  @Test
  void should_skip_projection_queries_when_category_ids_are_empty() {
    assertEquals(Map.of(), adapter.loadSelectionTypesByIds(List.of()));

    verifyNoInteractions(entityManager);
    verifyNoInteractions(optionGroupRepository);
    verifyNoInteractions(categoryMapper);
  }
}
