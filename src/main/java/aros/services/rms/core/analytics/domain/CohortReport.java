/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.List;

/**
 * Domain report for customer cohorts.
 *
 * <p>A "client" is an order visit. An order is recurring when the same fingerprint (phone hash with
 * a table fallback) was already seen inside the cohort window before that visit. The report exposes
 * the overall period plus one row per bucket key so the UI can render a retention table.
 */
public record CohortReport(
    PeriodInfo period,
    int newClients,
    int recurringClients,
    int totalOrders,
    BigDecimal recurringRatePct,
    Money averageTicketPerOrder,
    Money averageTicketPerCover,
    String fingerprintStrategy,
    int cohortWindowDays,
    List<CohortWindow> cohorts,
    String dataCompleteness,
    List<String> notes) {

  /** Describes the queried time range. */
  public record PeriodInfo(String bucket, String from, String to, List<String> keys) {}

  /** One bucket key inside the requested range. */
  public record CohortWindow(
      String key,
      int newClients,
      int recurringClients,
      int totalOrders,
      BigDecimal recurringRatePct) {}
}
