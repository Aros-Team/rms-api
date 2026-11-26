/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AnalyticsPeriods;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.CacheStatus;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MedianInfo;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.PeriodInfo;
import aros.services.rms.core.analytics.domain.port.in.GetMenuEngineeringUseCase;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.util.ArrayList;
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

  private static final long TTL_SECONDS = 86400;

  private final MenuEngineeringCacheRepositoryPort cacheRepo;
  private final CurrencyProvider currencyProvider;

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
          new MedianInfo(0, Money.zero(currencyProvider.getCurrency())),
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
    return new MedianInfo(medianVolume, new Money(medianMargin, currencyProvider.getCurrency()));
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
    return new CacheStatus(cacheRepo.findLatestUpdatedAt(), sourceVersion, TTL_SECONDS);
  }

  private MenuEngineeringReport emptyReport(String bucket, String from, String to, String note) {
    PeriodInfo period = new PeriodInfo(bucket, from, to, List.of());
    return new MenuEngineeringReport(
        period,
        new MedianInfo(0, Money.zero(currencyProvider.getCurrency())),
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
          "Bucket inválido '" + bucket + "'. Debe ser uno de: daily, weekly, monthly, yearly.");
    }
  }

  static List<String> generatePeriodKeys(String bucket, String from, String to) {
    // Single source of truth for bucket keys (daily/weekly/monthly/yearly).
    return AnalyticsPeriods.validateAndGenerate(bucket, from, to);
  }
}
