/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Integration-style tests for {@link RefreshMenuEngineeringService} with mocked EntityManager and
 * Query. Each test uses independent inline mocks (no shared state).
 *
 * <p>NOTE: {@code List.of(array)} with a single Object[] containing null elements can trigger
 * varargs-unpacking NPE on some JDK versions, so we use {@link java.util.Collections#singletonList}
 * for single-element arrays.
 */
class RefreshMenuEngineeringServiceTest {

  /** 3 active products with sales and recipes -> BCG computed correctly. */
  @Test
  void shouldComputeBcgFor3Products() {
    EntityManager em = mock(EntityManager.class);
    MenuEngineeringCacheRepositoryPort cacheRepo = mock(MenuEngineeringCacheRepositoryPort.class);
    Query productsQuery = mock(Query.class);
    Query salesQuery = mock(Query.class);
    Query recipeQuery = mock(Query.class);

    // product rows: id, name, base_price, category_id, category_name
    Object[] p1 = {1L, "P1", 20000.0, 1L, "A"};
    Object[] p2 = {2L, "P2", 15000.0, 1L, "A"};
    Object[] p3 = {3L, "P3", 5000.0, 2L, "B"};

    // sales rows: product_id, units_sold, revenue
    Object[] s1 = {1L, 10L, 200000.0};
    Object[] s2 = {2L, 20L, 300000.0};
    Object[] s3 = {3L, 5L, 25000.0};

    // recipe rows: product_id, recipe_cost
    Object[] r1 = {1L, BigDecimal.valueOf(3000.00)};
    Object[] r2 = {2L, BigDecimal.valueOf(10000.00)};
    Object[] r3 = {3L, BigDecimal.valueOf(2000.00)};

    when(em.createNativeQuery(anyString()))
        .thenReturn(productsQuery)
        .thenReturn(salesQuery)
        .thenReturn(recipeQuery);
    when(productsQuery.getResultList()).thenReturn(List.of(p1, p2, p3));
    when(salesQuery.setParameter(anyString(), any())).thenReturn(salesQuery);
    when(salesQuery.getResultList()).thenReturn(List.of(s1, s2, s3));
    when(recipeQuery.getResultList()).thenReturn(List.of(r1, r2, r3));

    RefreshMenuEngineeringService service = new RefreshMenuEngineeringService(em, cacheRepo);
    service.refresh("monthly", "2026-07");

    // Medians: volume [5,10,20] → 10, margin [3000,5000,17000] → 5000
    // P1: vol=10≥10 ✓, gp=17000≥5000 ✓ → STAR
    // P2: vol=20≥10 ✓, gp=5000≥5000 ✓ → STAR
    // P3: vol=5<10 ✗, gp=3000<5000 ✗ → DOG
    ArgumentCaptor<MenuItemSummary> captor = ArgumentCaptor.forClass(MenuItemSummary.class);
    verify(cacheRepo, times(3)).upsert(captor.capture(), anyString(), anyString(), anyString());

    List<MenuItemSummary> allItems = captor.getAllValues();
    assertEquals(3, allItems.size());
    assertEquals(1L, allItems.get(0).productId());
    assertEquals(BcgQuadrant.STAR, allItems.get(0).quadrant());
    assertEquals(2L, allItems.get(1).productId());
    assertEquals(BcgQuadrant.STAR, allItems.get(1).quadrant());
    assertEquals(3L, allItems.get(2).productId());
    assertEquals(BcgQuadrant.DOG, allItems.get(2).quadrant());
  }

  /** No active products -> no upsert. */
  @Test
  void shouldHandleNoActiveProducts() {
    EntityManager em = mock(EntityManager.class);
    MenuEngineeringCacheRepositoryPort cacheRepo = mock(MenuEngineeringCacheRepositoryPort.class);
    Query query = mock(Query.class);

    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of());

    RefreshMenuEngineeringService service = new RefreshMenuEngineeringService(em, cacheRepo);
    service.refresh("monthly", "2026-07");

    verify(cacheRepo, never()).upsert(any(), anyString(), anyString(), anyString());
  }

  /** Product exists but no sales and no recipe -> zeros and DOG. */
  @Test
  void shouldHandleNoSalesAndNoRecipes() {
    EntityManager em = mock(EntityManager.class);
    MenuEngineeringCacheRepositoryPort cacheRepo = mock(MenuEngineeringCacheRepositoryPort.class);
    Query productsQuery = mock(Query.class);
    Query salesQuery = mock(Query.class);
    Query recipeQuery = mock(Query.class);

    // product row with null categoryId and empty categoryName
    // NOTE: Collections.singletonList avoids varargs-unpacking NPE from List.of(Object[])
    Object[] p1 = {1L, "P1", 10000.0, null, ""};

    when(em.createNativeQuery(anyString()))
        .thenReturn(productsQuery)
        .thenReturn(salesQuery)
        .thenReturn(recipeQuery);
    when(productsQuery.getResultList()).thenReturn(java.util.Collections.singletonList(p1));
    when(salesQuery.setParameter(anyString(), any())).thenReturn(salesQuery);
    when(salesQuery.getResultList()).thenReturn(List.of());
    when(recipeQuery.getResultList()).thenReturn(List.of());

    RefreshMenuEngineeringService service = new RefreshMenuEngineeringService(em, cacheRepo);
    service.refresh("monthly", "2026-07");

    ArgumentCaptor<MenuItemSummary> captor = ArgumentCaptor.forClass(MenuItemSummary.class);
    verify(cacheRepo).upsert(captor.capture(), anyString(), anyString(), anyString());

    MenuItemSummary item = captor.getValue();
    assertEquals(1L, item.productId());
    assertEquals(0, item.unitsSold());
    // Single product = equals both medians → STAR
    assertEquals(BcgQuadrant.STAR, item.quadrant());
  }
}
