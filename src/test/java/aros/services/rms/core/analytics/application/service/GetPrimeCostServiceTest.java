/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.PrimeCostReport;
import aros.services.rms.core.analytics.domain.PrimeCostReport.PrimeCostSeries;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link GetPrimeCostService}. Covers period generation, missing data, empty ranges,
 * and validation.
 */
@ExtendWith(MockitoExtension.class)
class GetPrimeCostServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private MonthlyFinancialSummaryRepositoryPort summaryRepo;

  private GetPrimeCostService service() {
    return new GetPrimeCostService(summaryRepo);
  }

  // ---------------------------------------------------------------------------
  // G-01: returns series for monthly bucket
  // ---------------------------------------------------------------------------

  @Test
  void should_return_monthly_series() {
    MonthlyFinancialSummary jan = monthlySummary("2026-01", "FULL");
    MonthlyFinancialSummary feb = monthlySummary("2026-02", "FULL");
    when(summaryRepo.findByBucketAndPeriodKeyBetween("monthly", "2026-01", "2026-03"))
        .thenReturn(List.of(jan, feb));

    PrimeCostReport report = service().execute("monthly", "2026-01", "2026-03");

    assertNotNull(report);
    assertEquals("monthly", report.period().bucket());
    assertEquals("2026-01", report.period().from());
    assertEquals("2026-03", report.period().to());
    // 3 periods expected: 2026-01, 2026-02, 2026-03
    assertEquals(3, report.series().size());
    // Jan and Feb have FULL, Mar is EMPTY (not in persisted)
    assertEquals("FULL", report.series().get(0).dataCompleteness());
    assertEquals("FULL", report.series().get(1).dataCompleteness());
    assertEquals("EMPTY", report.series().get(2).dataCompleteness());
    assertEquals("PARTIAL", report.dataCompleteness());
  }

  // ---------------------------------------------------------------------------
  // G-02: returns series for daily bucket
  // ---------------------------------------------------------------------------

  @Test
  void should_return_daily_series() {
    when(summaryRepo.findByBucketAndPeriodKeyBetween("daily", "2026-07-01", "2026-07-03"))
        .thenReturn(List.of(monthlySummary("2026-07-02", "FULL")));

    PrimeCostReport report = service().execute("daily", "2026-07-01", "2026-07-03");

    assertEquals(3, report.series().size());
    assertEquals("2026-07-01", report.series().get(0).key());
    assertEquals("EMPTY", report.series().get(0).dataCompleteness());
    assertEquals("2026-07-02", report.series().get(1).key());
    assertEquals("FULL", report.series().get(1).dataCompleteness());
    assertEquals("2026-07-03", report.series().get(2).key());
    assertEquals("EMPTY", report.series().get(2).dataCompleteness());
  }

  // ---------------------------------------------------------------------------
  // G-03: returns series for weekly bucket
  // ---------------------------------------------------------------------------

  @Test
  void should_return_weekly_series() {
    // Use a short single-week range to avoid ISO week boundary issues
    when(summaryRepo.findByBucketAndPeriodKeyBetween("weekly", "2026-W28", "2026-W28"))
        .thenReturn(List.of());

    PrimeCostReport report = service().execute("weekly", "2026-W28", "2026-W28");

    assertNotNull(report);
    assertEquals(1, report.series().size());
    assertEquals("EMPTY", report.dataCompleteness());
  }

  // ---------------------------------------------------------------------------
  // G-04: returns series for yearly bucket
  // ---------------------------------------------------------------------------

  @Test
  void should_return_yearly_series() {
    when(summaryRepo.findByBucketAndPeriodKeyBetween("yearly", "2024", "2026"))
        .thenReturn(List.of());

    PrimeCostReport report = service().execute("yearly", "2024", "2026");

    assertEquals(3, report.series().size());
    assertEquals("2024", report.series().get(0).key());
    assertEquals("2025", report.series().get(1).key());
    assertEquals("2026", report.series().get(2).key());
  }

  // ---------------------------------------------------------------------------
  // G-05: empty range → report with empty series + dataCompleteness=EMPTY
  // ---------------------------------------------------------------------------

  @Test
  void should_return_empty_report_when_no_data() {
    when(summaryRepo.findByBucketAndPeriodKeyBetween("monthly", "2026-01", "2026-01"))
        .thenReturn(List.of());

    PrimeCostReport report = service().execute("monthly", "2026-01", "2026-01");

    assertEquals(1, report.series().size());
    assertEquals("EMPTY", report.series().get(0).dataCompleteness());
    assertEquals("EMPTY", report.dataCompleteness());
    assertEquals(1, report.notes().size());
  }

  // ---------------------------------------------------------------------------
  // G-06: COGS breakdown percentages computed correctly
  // ---------------------------------------------------------------------------

  @Test
  void should_compute_cogs_percentages() {
    MonthlyFinancialSummary summary =
        MonthlyFinancialSummary.builder()
            .periodKey("2026-07")
            .bucket("monthly")
            .cogsFood(new Money(new BigDecimal("30000000.00"), COP))
            .cogsBeverage(new Money(new BigDecimal("8000000.00"), COP))
            .cogsAlcohol(new Money(new BigDecimal("4000000.00"), COP))
            .cogsOther(new Money(new BigDecimal("2000000.00"), COP))
            .netSales(new Money(new BigDecimal("125000000.00"), COP))
            .grossSales(new Money(new BigDecimal("125000000.00"), COP))
            .laborFoh(Money.zero(COP))
            .laborBoh(Money.zero(COP))
            .laborTotal(Money.zero(COP))
            .primeCost(new Money(new BigDecimal("44000000.00"), COP))
            .primeCostPct(new BigDecimal("35.20"))
            .dataCompleteness("FULL")
            .build();

    when(summaryRepo.findByBucketAndPeriodKeyBetween("monthly", "2026-07", "2026-07"))
        .thenReturn(List.of(summary));

    PrimeCostReport report = service().execute("monthly", "2026-07", "2026-07");

    PrimeCostSeries series = report.series().get(0);
    var byCategory = series.cogs().byCategory();

    // Total COGS = 30M + 8M + 4M + 2M = 44M
    assertEquals(4, byCategory.size());

    // FOOD: 30/44 = 68.18%
    assertEquals("FOOD", byCategory.get(0).category());
    assertEquals(0, new BigDecimal("68.18").compareTo(byCategory.get(0).pct()));

    // BEVERAGE: 8/44 = 18.18%
    assertEquals("BEVERAGE", byCategory.get(1).category());
    assertEquals(0, new BigDecimal("18.18").compareTo(byCategory.get(1).pct()));

    // ALCOHOL: 4/44 = 9.09%
    assertEquals("ALCOHOL", byCategory.get(2).category());
    assertEquals(0, new BigDecimal("9.09").compareTo(byCategory.get(2).pct()));
  }

  // ---------------------------------------------------------------------------
  // G-07: invalid bucket throws
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_invalid_bucket() {
    GetPrimeCostService svc = service();
    assertThrows(
        IllegalArgumentException.class, () -> svc.execute("unknown", "2026-01", "2026-07"));
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static MonthlyFinancialSummary monthlySummary(String periodKey, String completeness) {
    return MonthlyFinancialSummary.builder()
        .periodKey(periodKey)
        .bucket("monthly")
        .netSales(new Money(new BigDecimal("10000000.00"), COP))
        .grossSales(new Money(new BigDecimal("10000000.00"), COP))
        .cogsFood(new Money(new BigDecimal("3000000.00"), COP))
        .cogsBeverage(new Money(new BigDecimal("1000000.00"), COP))
        .cogsAlcohol(Money.zero(COP))
        .cogsOther(Money.zero(COP))
        .laborFoh(new Money(new BigDecimal("2000000.00"), COP))
        .laborBoh(new Money(new BigDecimal("1500000.00"), COP))
        .laborTotal(new Money(new BigDecimal("3500000.00"), COP))
        .primeCost(new Money(new BigDecimal("7500000.00"), COP))
        .primeCostPct(new BigDecimal("75.00"))
        .dataCompleteness(completeness)
        .build();
  }
}
