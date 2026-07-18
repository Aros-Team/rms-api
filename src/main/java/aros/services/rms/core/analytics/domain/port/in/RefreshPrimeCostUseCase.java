/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import java.time.LocalDate;

/** Input port for refreshing (recomputing) the prime cost aggregate for a given date. */
public interface RefreshPrimeCostUseCase {

  /**
   * Recomputes the daily prime cost aggregates for the given date and upserts the result into the
   * monthly_financial_summary table.
   *
   * @param date the date to aggregate (yesterday in production)
   * @return the upserted summary row
   */
  MonthlyFinancialSummary refreshForDate(LocalDate date);
}
