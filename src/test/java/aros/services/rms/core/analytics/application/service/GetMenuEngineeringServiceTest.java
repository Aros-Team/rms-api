/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link GetMenuEngineeringService}. Covers period generation, missing data, and
 * validation.
 */
@ExtendWith(MockitoExtension.class)
class GetMenuEngineeringServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private MenuEngineeringCacheRepositoryPort cacheRepo;

  private GetMenuEngineeringService service() {
    return new GetMenuEngineeringService(cacheRepo);
  }

  // ---------------------------------------------------------------------------
  // G-01: returns report for monthly bucket with items
  // ---------------------------------------------------------------------------

  @Test
  void should_return_monthly_report_with_items() {
    MenuItemSummary item1 =
        new MenuItemSummary(
            1L,
            "P1",
            1L,
            "Cats",
            10,
            Money.of("50000.00", COP),
            Money.of("15000.00", COP),
            Money.of("35000.00", COP),
            Money.of("350000.00", COP),
            BcgQuadrant.STAR);
    MenuItemSummary item2 =
        new MenuItemSummary(
            2L,
            "P2",
            1L,
            "Cats",
            5,
            Money.of("25000.00", COP),
            Money.of("10000.00", COP),
            Money.of("15000.00", COP),
            Money.of("75000.00", COP),
            BcgQuadrant.DOG);

    when(cacheRepo.findByBucketAndPeriodKeyBetween("monthly", "2026-07", "2026-07", null))
        .thenReturn(List.of(item1, item2));
    when(cacheRepo.findLatestSourceVersion()).thenReturn("v1");

    MenuEngineeringReport report = service().execute("monthly", "2026-07", "2026-07", null);

    assertNotNull(report);
    assertEquals(1, report.period().keys().size());
    assertEquals("2026-07", report.period().keys().get(0));
    assertEquals(2, report.items().size());
    assertEquals("FULL", report.dataCompleteness());
  }

  // ---------------------------------------------------------------------------
  // G-02: empty cache returns EMPTY completeness
  // ---------------------------------------------------------------------------

  @Test
  void should_return_empty_when_cache_has_no_data() {
    when(cacheRepo.findByBucketAndPeriodKeyBetween("monthly", "2026-01", "2026-01", null))
        .thenReturn(List.of());
    when(cacheRepo.findLatestSourceVersion()).thenReturn("v1");

    MenuEngineeringReport report = service().execute("monthly", "2026-01", "2026-01", null);

    assertEquals("EMPTY", report.dataCompleteness());
    assertEquals(0, report.items().size());
    assertEquals(1, report.notes().size());
  }

  // ---------------------------------------------------------------------------
  // G-03: invalid bucket throws
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_invalid_bucket() {
    GetMenuEngineeringService svc = service();
    assertThrows(
        IllegalArgumentException.class, () -> svc.execute("unknown", "2026-01", "2026-07", null));
  }

  // ---------------------------------------------------------------------------
  // G-04: filters by categoryId
  // ---------------------------------------------------------------------------

  @Test
  void should_filter_by_category() {
    when(cacheRepo.findByBucketAndPeriodKeyBetween("monthly", "2026-07", "2026-07", 1L))
        .thenReturn(List.of());
    when(cacheRepo.findLatestSourceVersion()).thenReturn("v1");

    MenuEngineeringReport report = service().execute("monthly", "2026-07", "2026-07", 1L);

    assertNotNull(report);
    assertEquals("EMPTY", report.dataCompleteness());
  }
}
