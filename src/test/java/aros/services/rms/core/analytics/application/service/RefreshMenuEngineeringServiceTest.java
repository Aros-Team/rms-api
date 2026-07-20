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
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort.ActiveProduct;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort.SalesData;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RefreshMenuEngineeringServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Test
  void shouldComputeBcgFor3Products() {
    MenuEngineeringAggregationPort aggregationPort = mock(MenuEngineeringAggregationPort.class);
    MenuEngineeringCacheRepositoryPort cacheRepo = mock(MenuEngineeringCacheRepositoryPort.class);

    List<ActiveProduct> products =
        List.of(
            new ActiveProduct(1L, "P1", new Money(BigDecimal.valueOf(20000), COP), 1L, "A"),
            new ActiveProduct(2L, "P2", new Money(BigDecimal.valueOf(15000), COP), 1L, "A"),
            new ActiveProduct(3L, "P3", new Money(BigDecimal.valueOf(5000), COP), 2L, "B"));

    List<SalesData> sales =
        List.of(
            new SalesData(1L, 10, new Money(BigDecimal.valueOf(200000), COP)),
            new SalesData(2L, 20, new Money(BigDecimal.valueOf(300000), COP)),
            new SalesData(3L, 5, new Money(BigDecimal.valueOf(25000), COP)));

    Map<Long, Money> recipeCosts =
        Map.of(
            1L, new Money(BigDecimal.valueOf(3000), COP),
            2L, new Money(BigDecimal.valueOf(10000), COP),
            3L, new Money(BigDecimal.valueOf(2000), COP));

    when(aggregationPort.loadActiveProducts()).thenReturn(products);
    when(aggregationPort.loadSalesByProduct(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(sales);
    when(aggregationPort.loadRecipeCostByProduct()).thenReturn(recipeCosts);

    RefreshMenuEngineeringService service =
        new RefreshMenuEngineeringService(aggregationPort, cacheRepo);
    service.refresh("monthly", "2026-07");

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

  @Test
  void shouldHandleNoActiveProducts() {
    MenuEngineeringAggregationPort aggregationPort = mock(MenuEngineeringAggregationPort.class);
    MenuEngineeringCacheRepositoryPort cacheRepo = mock(MenuEngineeringCacheRepositoryPort.class);

    when(aggregationPort.loadActiveProducts()).thenReturn(List.of());

    RefreshMenuEngineeringService service =
        new RefreshMenuEngineeringService(aggregationPort, cacheRepo);
    service.refresh("monthly", "2026-07");

    verify(cacheRepo, never()).upsert(any(), anyString(), anyString(), anyString());
  }

  @Test
  void shouldHandleNoSalesAndNoRecipes() {
    MenuEngineeringAggregationPort aggregationPort = mock(MenuEngineeringAggregationPort.class);
    MenuEngineeringCacheRepositoryPort cacheRepo = mock(MenuEngineeringCacheRepositoryPort.class);

    List<ActiveProduct> products =
        List.of(new ActiveProduct(1L, "P1", new Money(BigDecimal.valueOf(10000), COP), null, ""));

    when(aggregationPort.loadActiveProducts()).thenReturn(products);
    when(aggregationPort.loadSalesByProduct(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(aggregationPort.loadRecipeCostByProduct()).thenReturn(Map.of());

    RefreshMenuEngineeringService service =
        new RefreshMenuEngineeringService(aggregationPort, cacheRepo);
    service.refresh("monthly", "2026-07");

    ArgumentCaptor<MenuItemSummary> captor = ArgumentCaptor.forClass(MenuItemSummary.class);
    verify(cacheRepo).upsert(captor.capture(), anyString(), anyString(), anyString());

    MenuItemSummary item = captor.getValue();
    assertEquals(1L, item.productId());
    assertEquals(0, item.unitsSold());
    assertEquals(BcgQuadrant.STAR, item.quadrant());
  }
}
