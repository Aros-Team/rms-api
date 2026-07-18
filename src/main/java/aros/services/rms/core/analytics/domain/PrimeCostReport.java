/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.List;

/** Domain report for prime cost & margins — mirrors the A6 contract shape. */
public record PrimeCostReport(
    Period period, List<PrimeCostSeries> series, String dataCompleteness, List<String> notes) {

  /** Describes the queried time range. */
  public record Period(String bucket, String from, String to, List<String> keys) {}

  /** One data point in the time series. */
  public record PrimeCostSeries(
      String key,
      Money netSales,
      Money grossSales,
      Money discounts,
      Money comped,
      CogsBreakdown cogs,
      LaborBreakdown labor,
      Money primeCost,
      BigDecimal primeCostPct,
      Margins margins,
      String dataCompleteness) {}

  /** COGS broken down by food type category. */
  public record CogsBreakdown(Money total, List<CogsCategory> byCategory) {}

  /** One COGS category line. */
  public record CogsCategory(String category, Money amount, BigDecimal pct) {}

  /** Labor broken down by area group. */
  public record LaborBreakdown(Money total, List<LaborArea> byArea) {}

  /** One labor area line. */
  public record LaborArea(String area, Money amount, BigDecimal pct) {}

  /** Percentages for gross and net profit. */
  public record Margins(BigDecimal grossProfitPct, BigDecimal netProfitPct) {}
}
