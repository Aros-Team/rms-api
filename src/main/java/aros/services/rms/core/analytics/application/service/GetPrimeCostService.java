/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.PrimeCostReport;
import aros.services.rms.core.analytics.domain.PrimeCostReport.CogsBreakdown;
import aros.services.rms.core.analytics.domain.PrimeCostReport.CogsCategory;
import aros.services.rms.core.analytics.domain.PrimeCostReport.LaborArea;
import aros.services.rms.core.analytics.domain.PrimeCostReport.LaborBreakdown;
import aros.services.rms.core.analytics.domain.PrimeCostReport.Margins;
import aros.services.rms.core.analytics.domain.PrimeCostReport.Period;
import aros.services.rms.core.analytics.domain.PrimeCostReport.PrimeCostSeries;
import aros.services.rms.core.analytics.domain.port.in.GetPrimeCostUseCase;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link GetPrimeCostUseCase} that reads from the monthly_financial_summary table
 * and fills in missing periods with empty data.
 */
@Service
@RequiredArgsConstructor
public class GetPrimeCostService implements GetPrimeCostUseCase {

  private static final Currency COP = Currency.getInstance("COP");
  private static final long MAX_RANGE_DAYS = 366;

  private final MonthlyFinancialSummaryRepositoryPort summaryRepo;

  /** {@inheritDoc} */
  @Override
  public PrimeCostReport execute(String bucket, String from, String to) {
    validateBucket(bucket);
    validateRange(bucket, from, to);

    List<String> expectedKeys = generatePeriodKeys(bucket, from, to);
    List<MonthlyFinancialSummary> persisted =
        summaryRepo.findByBucketAndPeriodKeyBetween(bucket, from, to);

    Set<String> persistedKeys =
        persisted.stream().map(MonthlyFinancialSummary::getPeriodKey).collect(Collectors.toSet());

    List<PrimeCostSeries> series = new ArrayList<>();
    List<String> notes = new ArrayList<>();
    boolean allFull = true;
    boolean anyFound = false;

    for (String key : expectedKeys) {
      if (persistedKeys.contains(key)) {
        MonthlyFinancialSummary s =
            persisted.stream().filter(p -> p.getPeriodKey().equals(key)).findFirst().orElseThrow();
        series.add(toSeries(s));
        anyFound = true;
        if (!"FULL".equals(s.getDataCompleteness())) {
          allFull = false;
        }
      } else {
        // Generate empty period
        series.add(emptySeries(key));
        allFull = false;
      }
    }

    String dataCompleteness;
    if (!anyFound) {
      dataCompleteness = "EMPTY";
      notes.add("No financial data found for the requested range.");
    } else if (!allFull) {
      dataCompleteness = "PARTIAL";
      if (!persistedKeys.containsAll(expectedKeys)) {
        notes.add("Some periods have no data — values may be understated.");
      }
    } else {
      dataCompleteness = "FULL";
    }

    return new PrimeCostReport(
        new Period(bucket, from, to, expectedKeys), series, dataCompleteness, notes);
  }

  private PrimeCostSeries toSeries(MonthlyFinancialSummary s) {
    // COGS breakdown
    Money cogsFood = s.getCogsFood();
    Money cogsBeverage = s.getCogsBeverage();
    Money cogsAlcohol = s.getCogsAlcohol();
    Money cogsOther = s.getCogsOther();
    Money totalCogs = cogsFood.plus(cogsBeverage).plus(cogsAlcohol).plus(cogsOther);

    List<CogsCategory> cogsCategories = new ArrayList<>();
    addCogsCategory(cogsCategories, "FOOD", cogsFood, totalCogs);
    addCogsCategory(cogsCategories, "BEVERAGE", cogsBeverage, totalCogs);
    addCogsCategory(cogsCategories, "ALCOHOL", cogsAlcohol, totalCogs);
    addCogsCategory(cogsCategories, "OTHER", cogsOther, totalCogs);

    // Labor breakdown
    Money laborFoh = s.getLaborFoh();
    Money laborBoh = s.getLaborBoh();
    Money laborTotal = s.getLaborTotal();

    List<LaborArea> laborAreas = new ArrayList<>();
    addLaborArea(laborAreas, "FOH", laborFoh, laborTotal);
    addLaborArea(laborAreas, "BOH", laborBoh, laborTotal);

    Money primeCost = s.getPrimeCost();
    BigDecimal primeCostPct = s.getPrimeCostPct();

    Margins margins = new Margins(s.getGrossProfitPct(), s.getNetProfitPct());

    return new PrimeCostSeries(
        s.getPeriodKey(),
        s.getNetSales(),
        s.getGrossSales(),
        s.getDiscounts(),
        s.getComped(),
        new CogsBreakdown(totalCogs, cogsCategories),
        new LaborBreakdown(laborTotal, laborAreas),
        primeCost,
        primeCostPct,
        margins,
        s.getDataCompleteness());
  }

  private PrimeCostSeries emptySeries(String key) {
    Money zero = Money.zero(COP);
    List<CogsCategory> cogsCats =
        List.of(
            new CogsCategory("FOOD", zero, BigDecimal.ZERO),
            new CogsCategory("BEVERAGE", zero, BigDecimal.ZERO),
            new CogsCategory("ALCOHOL", zero, BigDecimal.ZERO),
            new CogsCategory("OTHER", zero, BigDecimal.ZERO));
    List<LaborArea> laborAreas =
        List.of(
            new LaborArea("FOH", zero, BigDecimal.ZERO),
            new LaborArea("BOH", zero, BigDecimal.ZERO));
    return new PrimeCostSeries(
        key,
        zero,
        zero,
        zero,
        zero,
        new CogsBreakdown(zero, cogsCats),
        new LaborBreakdown(zero, laborAreas),
        zero,
        null,
        new Margins(BigDecimal.ZERO, BigDecimal.ZERO),
        "EMPTY");
  }

  private static void addCogsCategory(
      List<CogsCategory> list, String name, Money amount, Money total) {
    BigDecimal pct = computePct(amount, total);
    list.add(new CogsCategory(name, amount, pct));
  }

  private static void addLaborArea(List<LaborArea> list, String name, Money amount, Money total) {
    BigDecimal pct = computePct(amount, total);
    list.add(new LaborArea(name, amount, pct));
  }

  private static BigDecimal computePct(Money numerator, Money denominator) {
    if (denominator.isZero()) {
      return BigDecimal.ZERO;
    }
    return numerator
        .amount()
        .multiply(new BigDecimal("100"))
        .divide(denominator.amount(), 2, java.math.RoundingMode.HALF_UP);
  }

  // ---------------------------------------------------------------------------
  // Period key generation
  // ---------------------------------------------------------------------------

  static List<String> generatePeriodKeys(String bucket, String from, String to) {
    List<String> keys = new ArrayList<>();
    switch (bucket) {
      case "daily" -> {
        LocalDate current = LocalDate.parse(from, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(to, DateTimeFormatter.ISO_LOCAL_DATE);
        while (!current.isAfter(end)) {
          keys.add(current.format(DateTimeFormatter.ISO_LOCAL_DATE));
          current = current.plusDays(1);
        }
      }
      case "weekly" -> {
        LocalDate fromDate = parseIsoWeek(from, 1); // Monday
        LocalDate toDate = parseIsoWeek(to, 7); // Sunday
        LocalDate current = fromDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        while (!current.isAfter(toDate)) {
          int year = current.get(IsoFields.WEEK_BASED_YEAR);
          int week = current.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
          keys.add(String.format("%04d-W%02d", year, week));
          current = current.plusWeeks(1);
        }
      }
      case "monthly" -> {
        String[] fromParts = from.split("-");
        String[] toParts = to.split("-");
        int year = Integer.parseInt(fromParts[0]);
        int month = Integer.parseInt(fromParts[1]);
        int endYear = Integer.parseInt(toParts[0]);
        int endMonth = Integer.parseInt(toParts[1]);
        while (year < endYear || (year == endYear && month <= endMonth)) {
          keys.add(String.format("%04d-%02d", year, month));
          month++;
          if (month > 12) {
            month = 1;
            year++;
          }
        }
      }
      case "yearly" -> {
        int fromYear = Integer.parseInt(from);
        int toYear = Integer.parseInt(to);
        for (int y = fromYear; y <= toYear; y++) {
          keys.add(String.valueOf(y));
        }
      }
      default -> throw new IllegalArgumentException("Unsupported bucket: " + bucket);
    }
    return keys;
  }

  /**
   * Parses an ISO week string like "2026-W28" plus a day-of-week number to a LocalDate.
   *
   * @param weekKey the week key in format "YYYY-Www"
   * @param dayOfWeek the day of week (1=Monday, 7=Sunday)
   * @return the corresponding LocalDate
   */
  private static LocalDate parseIsoWeek(String weekKey, int dayOfWeek) {
    DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(IsoFields.WEEK_BASED_YEAR, 4)
            .appendLiteral("-W")
            .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
            .appendLiteral("-")
            .appendValue(java.time.temporal.WeekFields.ISO.dayOfWeek(), 1)
            .toFormatter();
    return LocalDate.parse(weekKey + "-" + dayOfWeek, formatter);
  }

  // ---------------------------------------------------------------------------
  // Validation
  // ---------------------------------------------------------------------------

  private void validateBucket(String bucket) {
    if (!Set.of("daily", "weekly", "monthly", "yearly").contains(bucket)) {
      throw new IllegalArgumentException(
          "Invalid bucket '" + bucket + "'. Must be one of: daily, weekly, monthly, yearly.");
    }
  }

  private void validateRange(String bucket, String from, String to) {
    // Validate format
    switch (bucket) {
      case "daily" -> {
        validateDateFormat(from, DateTimeFormatter.ISO_LOCAL_DATE, "YYYY-MM-DD");
        validateDateFormat(to, DateTimeFormatter.ISO_LOCAL_DATE, "YYYY-MM-DD");
      }
      case "weekly" -> {
        // Validate by trying to parse
        try {
          parseIsoWeek(from, 1);
          parseIsoWeek(to, 7);
        } catch (DateTimeParseException e) {
          throw new IllegalArgumentException(
              "Invalid period format for weekly: '"
                  + from
                  + "' or '"
                  + to
                  + "'. Expected format: YYYY-Www");
        }
      }
      case "monthly" -> {
        validateDateFormat(from + "-01", DateTimeFormatter.ISO_LOCAL_DATE, "YYYY-MM");
        validateDateFormat(to + "-01", DateTimeFormatter.ISO_LOCAL_DATE, "YYYY-MM");
      }
      case "yearly" -> {
        validateDateFormat(from + "-01-01", DateTimeFormatter.ISO_LOCAL_DATE, "YYYY");
        validateDateFormat(to + "-01-01", DateTimeFormatter.ISO_LOCAL_DATE, "YYYY");
      }
      default -> throw new IllegalArgumentException("Unsupported bucket: " + bucket);
    }

    // Range validation
    List<String> keys = generatePeriodKeys(bucket, from, to);
    if (keys.isEmpty()) {
      throw new IllegalArgumentException("Range produced no periods: from=" + from + " to=" + to);
    }
    if (keys.size() > MAX_RANGE_DAYS && "daily".equals(bucket)) {
      throw new IllegalArgumentException(
          "Range exceeds " + MAX_RANGE_DAYS + " days: " + keys.size() + " days requested.");
    }
  }

  private void validateDateFormat(String value, DateTimeFormatter formatter, String expected) {
    try {
      LocalDate.parse(value, formatter);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid period format for bucket: '" + value + "'. Expected format: " + expected);
    }
  }
}
