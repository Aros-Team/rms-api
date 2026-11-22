/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link RollupPrimeCostService}.
 *
 * <p><b>Feature:</b> Idempotent daily-to-weekly/monthly/yearly prime cost rollup.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Two daily rows are summed into the monthly bucket
 *   <li>COGS, labor, prime cost and every percentage are recomputed
 *   <li>An empty daily range writes nothing
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks {@link MonthlyFinancialSummaryRepositoryPort} with two daily rows
 *   <li>Captures the upserted rows and selects the monthly one
 *   <li>Asserts the aggregated money values and percentages
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RollupPrimeCostServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private MonthlyFinancialSummaryRepositoryPort summaryRepositoryPort;
  @Mock private CurrencyProvider currencyProvider;

  @Test
  void should_roll_daily_rows_into_monthly_bucket() {
    when(currencyProvider.getCurrency()).thenReturn(COP);
    when(summaryRepositoryPort.findByBucketAndPeriodKeyBetween("daily", "2026-11-02", "2026-11-03"))
        .thenReturn(
            List.of(
                daily("2026-11-02", "100000.00", "30000.00", "10000.00"),
                daily("2026-11-03", "100000.00", "30000.00", "10000.00")));

    int written =
        new RollupPrimeCostService(summaryRepositoryPort, currencyProvider)
            .rollup(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 3));

    assertEquals(3, written);
    ArgumentCaptor<MonthlyFinancialSummary> captor =
        ArgumentCaptor.forClass(MonthlyFinancialSummary.class);
    verify(summaryRepositoryPort, atLeastOnce()).upsert(captor.capture());

    MonthlyFinancialSummary monthly =
        captor.getAllValues().stream()
            .filter(row -> "monthly".equals(row.getBucket()))
            .findFirst()
            .orElseThrow();
    assertEquals("2026-11", monthly.getPeriodKey());
    assertEquals("200000.00", monthly.getNetSales().amount().toPlainString());
    assertEquals("60000.00", monthly.getCogsFood().amount().toPlainString());
    assertEquals("20000.00", monthly.getLaborTotal().amount().toPlainString());
    assertEquals("80000.00", monthly.getPrimeCost().amount().toPlainString());
    assertEquals("40.00", monthly.getPrimeCostPct().toPlainString());
    assertEquals("70.00", monthly.getGrossProfitPct().toPlainString());
    assertEquals("60.00", monthly.getNetProfitPct().toPlainString());
    assertEquals("PARTIAL", monthly.getDataCompleteness());
  }

  @Test
  void should_be_noop_when_no_daily_rows_exist() {
    when(summaryRepositoryPort.findByBucketAndPeriodKeyBetween("daily", "2026-12-01", "2026-12-31"))
        .thenReturn(List.of());

    int written =
        new RollupPrimeCostService(summaryRepositoryPort, currencyProvider)
            .rollup(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));

    assertEquals(0, written);
    verify(summaryRepositoryPort, org.mockito.Mockito.never()).upsert(any());
  }

  private static MonthlyFinancialSummary daily(
      String key, String net, String cogsFood, String labor) {
    BigDecimal netAmount = new BigDecimal(net);
    BigDecimal cogs = new BigDecimal(cogsFood);
    BigDecimal laborAmount = new BigDecimal(labor);
    return MonthlyFinancialSummary.builder()
        .periodKey(key)
        .bucket("daily")
        .netSales(new Money(netAmount, COP))
        .grossSales(new Money(netAmount, COP))
        .discounts(Money.zero(COP))
        .comped(Money.zero(COP))
        .cogsFood(new Money(cogs, COP))
        .cogsBeverage(Money.zero(COP))
        .cogsAlcohol(Money.zero(COP))
        .cogsOther(Money.zero(COP))
        .laborFoh(new Money(laborAmount, COP))
        .laborBoh(Money.zero(COP))
        .laborTotal(new Money(laborAmount, COP))
        .primeCost(new Money(cogs.add(laborAmount), COP))
        .dataCompleteness("FULL")
        .build();
  }
}
