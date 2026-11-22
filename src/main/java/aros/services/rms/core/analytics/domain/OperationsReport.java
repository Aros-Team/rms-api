/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.List;

/**
 * Domain report for operational efficiency (RevPASH, covers, table turns and labor share).
 *
 * <p>All rates are computed over the requested period. {@code totalAvailableSeatHours} is the
 * theoretical capacity of the floor: every table seat for every operating minute in the range.
 */
public record OperationsReport(
    PeriodInfo period,
    RevPash revPash,
    TableTurnover tableTurnover,
    LaborShare labor,
    long covers,
    long orders,
    BigDecimal avgOccupancyMinutes,
    long totalOccupiedSeatMinutes,
    String dataCompleteness,
    List<String> notes) {

  /** Describes the queried time range. */
  public record PeriodInfo(String bucket, String from, String to, List<String> keys) {}

  /** Revenue per available seat hour. */
  public record RevPash(Money value, Money totalNetSales, BigDecimal totalAvailableSeatHours) {}

  /** Table turns overall and per day part. */
  public record TableTurnover(BigDecimal overall, List<DayPartTurnover> byDayPart) {}

  /** Table turns for one day part. */
  public record DayPartTurnover(String dayPart, BigDecimal turns, long covers, long tables) {}

  /** Labor cost and its share of net sales. */
  public record LaborShare(Money totalCost, BigDecimal sharePct) {}
}
