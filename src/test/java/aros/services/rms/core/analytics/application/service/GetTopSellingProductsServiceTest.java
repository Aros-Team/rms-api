/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.TopSellingProduct;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsRangeException;
import aros.services.rms.core.analytics.domain.port.out.TopSellingQueryPort;
import aros.services.rms.core.analytics.domain.port.out.TopSellingQueryPort.ProductSales;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link GetTopSellingProductsService}.
 *
 * <p><b>Feature:</b> Top-selling products report (D-014): ranking by units sold with revenue.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Products are ranked in the order returned by the query port
 *   <li>The limit is clamped to the 1..100 range and the end date is exclusive
 *   <li>An inverted date range is rejected
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks {@link TopSellingQueryPort} with two product aggregates
 *   <li>Executes the service with a single-month range
 *   <li>Asserts rank assignment, units and the exclusive end-date argument
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class GetTopSellingProductsServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private TopSellingQueryPort topSellingQueryPort;

  @Test
  void should_rank_products_and_use_exclusive_end_date() {
    when(topSellingQueryPort.findTopSelling(
            LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1), 10))
        .thenReturn(
            List.of(
                new ProductSales(
                    1L, "Burger", 4691, new Money(new BigDecimal("131348000.00"), COP)),
                new ProductSales(
                    5L, "Lemonade", 5820, new Money(new BigDecimal("46560000.00"), COP))));

    List<TopSellingProduct> result =
        new GetTopSellingProductsService(topSellingQueryPort)
            .execute(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 10);

    assertEquals(2, result.size());
    assertEquals(1, result.get(0).rank());
    assertEquals(2, result.get(1).rank());
    assertEquals("Burger", result.get(0).productName());
    assertEquals(4691, result.get(0).unitsSold());
  }

  @Test
  void should_clamp_limit_to_maximum() {
    when(topSellingQueryPort.findTopSelling(any(), any(), org.mockito.ArgumentMatchers.eq(100)))
        .thenReturn(List.of());

    new GetTopSellingProductsService(topSellingQueryPort)
        .execute(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 500);
  }

  @Test
  void should_reject_inverted_range() {
    assertThrows(
        InvalidAnalyticsRangeException.class,
        () ->
            new GetTopSellingProductsService(topSellingQueryPort)
                .execute(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1), 10));
  }
}
