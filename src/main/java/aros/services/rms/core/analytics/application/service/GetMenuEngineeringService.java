/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.MenuEngineeringReport;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.CacheStatus;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MedianInfo;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.PeriodInfo;
import aros.services.rms.core.analytics.domain.port.in.GetMenuEngineeringUseCase;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link GetMenuEngineeringUseCase} that reads from the menu_performance_cache
 * table and returns a BCG quadrant report for the requested period range.
 */
@Service
@RequiredArgsConstructor
public class GetMenuEngineeringService implements GetMenuEngineeringUseCase {

  private static final Currency COP = Currency.getInstance("COP");
  private static final long TTL_SECONDS = 86400;

  private final MenuEngineeringCacheRepositoryPort cacheRepo;

  /** {@inheritDoc} */
  @Override
  public MenuEngineeringReport execute(String bucket, String from, String to, Long categoryId) {
    validateBucket(bucket);

    List<String> keys = generatePeriodKeys(bucket, from, to);
    if (keys.isEmpty()) {
      return emptyReport(bucket, from, to, "No periods generated for the requested range.");
    }

    List<MenuItemSummary> cachedItems =
        cacheRepo.findByBucketAndPeriodKeyBetween(bucket, from, to, categoryId);

    // Build report
    PeriodInfo period = new PeriodInfo(bucket, from, to, keys);
    CacheStatus cacheStatus = buildCacheStatus();

    if (cachedItems.isEmpty()) {
      return new MenuEngineeringReport(
          period,
          new MedianInfo(0, Money.zero(COP)),
          List.of(),
          cacheStatus,
          "EMPTY",
          List.of("No menu engineering data found for the requested range."));
    }

    // Compute median from cached items
    MedianInfo median = computeMedian(cachedItems);

    String dataCompleteness = "FULL";

    return new MenuEngineeringReport(
        period, median, cachedItems, cacheStatus, dataCompleteness, List.of());
  }

  private MedianInfo computeMedian(List<MenuItemSummary> items) {
    List<Integer> volumes = new ArrayList<>();
    List<BigDecimal> margins = new ArrayList<>();
    for (MenuItemSummary item : items) {
      volumes.add(item.unitsSold());
      margins.add(item.grossProfitPerUnit().amount());
    }
    int medianVolume = medianInt(volumes);
    BigDecimal medianMargin = medianBigDecimal(margins);
    return new MedianInfo(medianVolume, new Money(medianMargin, COP));
  }

  private static int medianInt(List<Integer> values) {
    if (values.isEmpty()) {
      return 0;
    }
    List<Integer> sorted = new ArrayList<>(values);
    java.util.Collections.sort(sorted);
    return sorted.get(sorted.size() / 2);
  }

  private static BigDecimal medianBigDecimal(List<BigDecimal> values) {
    if (values.isEmpty()) {
      return BigDecimal.ZERO;
    }
    List<BigDecimal> sorted = new ArrayList<>(values);
    java.util.Collections.sort(sorted);
    return sorted.get(sorted.size() / 2);
  }

  private CacheStatus buildCacheStatus() {
    String sourceVersion = cacheRepo.findLatestSourceVersion();
    if (sourceVersion == null) {
      sourceVersion = "unknown";
    }
    return new CacheStatus(Instant.now(), sourceVersion, TTL_SECONDS);
  }

  private MenuEngineeringReport emptyReport(String bucket, String from, String to, String note) {
    PeriodInfo period = new PeriodInfo(bucket, from, to, List.of());
    return new MenuEngineeringReport(
        period,
        new MedianInfo(0, Money.zero(COP)),
        List.of(),
        buildCacheStatus(),
        "EMPTY",
        List.of(note));
  }

  // ---------------------------------------------------------------------------
  // Validation & helpers
  // ---------------------------------------------------------------------------

  private void validateBucket(String bucket) {
    if (!Set.of("daily", "weekly", "monthly", "yearly").contains(bucket)) {
      throw new IllegalArgumentException(
          "Invalid bucket '" + bucket + "'. Must be one of: daily, weekly, monthly, yearly.");
    }
  }

  static List<String> generatePeriodKeys(String bucket, String from, String to) {
    // Reuse same logic as GetPrimeCostService but simpler — just return the range
    List<String> keys = new ArrayList<>();
    switch (bucket) {
      case "daily" -> {
        var start = java.time.LocalDate.parse(from);
        var end = java.time.LocalDate.parse(to);
        while (!start.isAfter(end)) {
          keys.add(start.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
          start = start.plusDays(1);
        }
      }
      case "weekly" -> {
        var start =
            java.time.LocalDate.parse(
                from + "-1",
                new java.time.format.DateTimeFormatterBuilder()
                    .appendPattern("YYYY-'W'ww")
                    .parseDefaulting(java.time.temporal.ChronoField.DAY_OF_WEEK, 1)
                    .toFormatter());
        var end =
            java.time.LocalDate.parse(
                to + "-7",
                new java.time.format.DateTimeFormatterBuilder()
                    .appendPattern("YYYY-'W'ww")
                    .parseDefaulting(java.time.temporal.ChronoField.DAY_OF_WEEK, 7)
                    .toFormatter());
        while (!start.isAfter(end)) {
          int y = start.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR);
          int w = start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
          keys.add(String.format("%04d-W%02d", y, w));
          start = start.plusWeeks(1);
        }
      }
      case "monthly" -> {
        String[] fromParts = from.split("-");
        String[] toParts = to.split("-");
        int y = Integer.parseInt(fromParts[0]);
        int m = Integer.parseInt(fromParts[1]);
        int endY = Integer.parseInt(toParts[0]);
        int endM = Integer.parseInt(toParts[1]);
        while (y < endY || (y == endY && m <= endM)) {
          keys.add(String.format("%04d-%02d", y, m));
          m++;
          if (m > 12) {
            m = 1;
            y++;
          }
        }
      }
      case "yearly" -> {
        int fromY = Integer.parseInt(from);
        int toY = Integer.parseInt(to);
        for (int y = fromY; y <= toY; y++) {
          keys.add(String.valueOf(y));
        }
      }
      default -> throw new IllegalArgumentException("Unsupported bucket: " + bucket);
    }
    return keys;
  }
}
