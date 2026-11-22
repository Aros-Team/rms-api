/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Output port with the raw aggregates needed by the operations report. */
public interface OperationsQueryPort {

  /** Covers for one day part. */
  record DayPartCovers(String dayPart, long covers) {}

  /** One aggregate row for the requested range. */
  record OperationsSnapshot(
      long totalOrders,
      long ordersWithTimestamps,
      long covers,
      long occupiedMinutes,
      long occupiedSeatMinutes,
      Money netSales,
      List<DayPartCovers> coversByDayPart,
      long tableCount,
      long seatCount) {}

  /**
   * Loads the operations aggregate for a half-open date range.
   *
   * @param startInclusive inclusive start date
   * @param endExclusive exclusive end date
   * @param lunchStart lunch window start
   * @param lunchEnd lunch window end
   * @param dinnerStart dinner window start
   * @param dinnerEnd dinner window end
   * @return the aggregate snapshot
   */
  OperationsSnapshot load(
      LocalDate startInclusive,
      LocalDate endExclusive,
      LocalTime lunchStart,
      LocalTime lunchEnd,
      LocalTime dinnerStart,
      LocalTime dinnerEnd);

  /**
   * Loads the labor cost recorded in the daily financial rollups for a half-open date range.
   *
   * @param startInclusive inclusive start date
   * @param endExclusive exclusive end date
   * @return the labor cost, zero when no daily row exists
   */
  Money loadLaborCost(LocalDate startInclusive, LocalDate endExclusive);
}
