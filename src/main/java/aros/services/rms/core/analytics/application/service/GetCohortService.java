/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AnalyticsPeriods;
import aros.services.rms.core.analytics.domain.CohortReport;
import aros.services.rms.core.analytics.domain.CohortReport.CohortWindow;
import aros.services.rms.core.analytics.domain.CohortReport.PeriodInfo;
import aros.services.rms.core.analytics.domain.port.in.GetCohortUseCase;
import aros.services.rms.core.analytics.domain.port.out.CohortQueryPort;
import aros.services.rms.core.analytics.domain.port.out.CohortQueryPort.CohortOrderRow;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Computes customer cohorts with "a client is an order visit" semantics.
 *
 * <p>An order is <em>recurring</em> when the same fingerprint was already seen in the 90 days
 * before that visit. New clients are the remaining orders. The fingerprint is the normalized phone
 * hash, falling back to the table id when no phone was captured; the fallback is flagged as {@code
 * PARTIAL} because it over-counts repeat visits.
 */
@Service
@RequiredArgsConstructor
public class GetCohortService implements GetCohortUseCase {

  /** Length of the look-back window used to classify a visit as recurring. */
  static final int COHORT_WINDOW_DAYS = 90;

  private final CohortQueryPort cohortQueryPort;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public CohortReport execute(String bucket, String from, String to) {
    List<String> keys = AnalyticsPeriods.validateAndGenerate(bucket, from, to);
    LocalDate start = AnalyticsPeriods.start(bucket, from);
    LocalDate endExclusive = AnalyticsPeriods.endExclusive(bucket, to);

    List<CohortOrderRow> rows =
        cohortQueryPort.findOrders(
            start.minusDays(COHORT_WINDOW_DAYS).atStartOfDay(), endExclusive.atStartOfDay());

    final Currency currency = currencyProvider.getCurrency();
    Map<String, Deque<LocalDateTime>> visits = new HashMap<>();
    Map<String, int[]> cohorts = new LinkedHashMap<>();
    for (String key : keys) {
      cohorts.put(key, new int[3]); // new, recurring, total
    }

    int totalOrders = 0;
    int recurringClients = 0;
    int phoneOrders = 0;
    long covers = 0;
    BigDecimal totalSales = BigDecimal.ZERO;

    for (CohortOrderRow row : rows) {
      LocalDateTime visit = row.orderDate();
      String fingerprint =
          row.fingerprint() == null || row.fingerprint().isBlank()
              ? "unknown:" + row.id()
              : row.fingerprint();
      Deque<LocalDateTime> previous =
          visits.computeIfAbsent(fingerprint, key -> new ArrayDeque<>());
      LocalDateTime windowStart = visit.minusDays(COHORT_WINDOW_DAYS);
      while (!previous.isEmpty() && previous.peekFirst().isBefore(windowStart)) {
        previous.pollFirst();
      }

      boolean inPeriod =
          !visit.toLocalDate().isBefore(start) && visit.toLocalDate().isBefore(endExclusive);
      if (inPeriod) {
        final boolean recurring = !previous.isEmpty();
        String key = AnalyticsPeriods.keyFor(bucket, visit.toLocalDate());
        final int[] cohort = cohorts.computeIfAbsent(key, ignored -> new int[3]);
        totalOrders++;
        covers += row.covers();
        totalSales = totalSales.add(row.total() == null ? BigDecimal.ZERO : row.total().amount());
        if (row.hasPhone()) {
          phoneOrders++;
        }
        if (recurring) {
          recurringClients++;
          cohort[1]++;
        } else {
          cohort[0]++;
        }
        cohort[2]++;
      }

      previous.addLast(visit);
    }

    final int newClients = totalOrders - recurringClients;
    final BigDecimal recurringRatePct = pct(recurringClients, totalOrders);
    final BigDecimal averageTicketPerOrder = divide(totalSales, totalOrders);
    final BigDecimal averageTicketPerCover = divide(totalSales, covers);

    List<CohortWindow> cohortWindows = new ArrayList<>();
    for (Map.Entry<String, int[]> entry : cohorts.entrySet()) {
      int[] value = entry.getValue();
      cohortWindows.add(
          new CohortWindow(entry.getKey(), value[0], value[1], value[2], pct(value[1], value[2])));
    }

    String strategy;
    if (totalOrders == 0 || phoneOrders == 0) {
      strategy = "TABLE_ONLY";
    } else if (phoneOrders == totalOrders) {
      strategy = "PHONE";
    } else {
      strategy = "PHONE_OR_TABLE";
    }

    List<String> notes = new ArrayList<>();
    String completeness;
    if (totalOrders == 0) {
      completeness = "EMPTY";
      notes.add("No orders found for the requested range.");
    } else if (phoneOrders < totalOrders) {
      completeness = "PARTIAL";
      BigDecimal coverage = pct(phoneOrders, totalOrders);
      notes.add(
          "Phone number captured on "
              + coverage.toPlainString()
              + "% of orders in range; remaining rows fingerprint by table_id, so the recurring"
              + " rate is an upper bound.");
    } else {
      completeness = "FULL";
    }

    return new CohortReport(
        new PeriodInfo(bucket, from, to, keys),
        newClients,
        recurringClients,
        totalOrders,
        recurringRatePct,
        new Money(averageTicketPerOrder, currency),
        new Money(averageTicketPerCover, currency),
        strategy,
        COHORT_WINDOW_DAYS,
        cohortWindows,
        completeness,
        notes);
  }

  private static BigDecimal pct(int numerator, int denominator) {
    if (denominator == 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(numerator)
        .multiply(new BigDecimal("100"))
        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
  }

  private static BigDecimal divide(BigDecimal numerator, long denominator) {
    if (denominator == 0) {
      return BigDecimal.ZERO;
    }
    return numerator.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
  }
}
