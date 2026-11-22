/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.analytics.domain.exception.AnalyticsRangeTooLargeException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsPeriodException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsRangeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared period parsing, validation and key generation for every analytics module.
 *
 * <p>Supported buckets are {@code daily} ({@code YYYY-MM-DD}), {@code weekly} ({@code YYYY-Www},
 * ISO-8601 week), {@code monthly} ({@code YYYY-MM}) and {@code yearly} ({@code YYYY}). A range is
 * inclusive on both ends; the resolved date range is half-open ({@code start} inclusive, {@code
 * end} exclusive).
 */
public final class AnalyticsPeriods {

  /** The four buckets accepted everywhere a period is requested. */
  public static final Set<String> BUCKETS = Set.of("daily", "weekly", "monthly", "yearly");

  /** Maximum number of calendar days a single analytics range may span. */
  public static final long MAX_RANGE_DAYS = 366;

  private static final Pattern WEEKLY = Pattern.compile("\\d{4}-W\\d{2}");
  private static final Pattern MONTHLY = Pattern.compile("\\d{4}-\\d{2}");
  private static final Pattern YEARLY = Pattern.compile("\\d{4}");

  private AnalyticsPeriods() {}

  /**
   * Validates the bucket name.
   *
   * @param bucket the bucket name
   * @throws InvalidAnalyticsPeriodException when the bucket is not supported
   */
  public static void validateBucket(String bucket) {
    if (bucket == null || !BUCKETS.contains(bucket)) {
      throw new InvalidAnalyticsPeriodException(
          "Invalid bucket '" + bucket + "'. Must be one of: daily, weekly, monthly, yearly.");
    }
  }

  /**
   * Validates the range format, ordering and size, then returns the concrete period keys.
   *
   * @param bucket the bucket name
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the concrete period keys covered by the range
   * @throws InvalidAnalyticsPeriodException when a key does not match the bucket format
   * @throws InvalidAnalyticsRangeException when {@code to} is before {@code from}
   * @throws AnalyticsRangeTooLargeException when the range exceeds {@link #MAX_RANGE_DAYS}
   */
  public static List<String> validateAndGenerate(String bucket, String from, String to) {
    validateBucket(bucket);
    LocalDate start = start(bucket, from);
    LocalDate endExclusive = endExclusive(bucket, to);
    if (endExclusive.isBefore(start) || endExclusive.isEqual(start)) {
      throw new InvalidAnalyticsRangeException(
          "Range produced no periods: from=" + from + " to=" + to);
    }
    long days = ChronoUnit.DAYS.between(start, endExclusive);
    if ("daily".equals(bucket) && days > MAX_RANGE_DAYS) {
      throw new AnalyticsRangeTooLargeException(
          "Range exceeds "
              + MAX_RANGE_DAYS
              + " days: "
              + days
              + " days requested (from="
              + from
              + ", to="
              + to
              + ").");
    }
    return keys(bucket, start, endExclusive);
  }

  /**
   * Generates the period keys covered by the range without validating format or size. Callers that
   * accept user input should use {@link #validateAndGenerate(String, String, String)} instead.
   *
   * @param bucket the bucket name
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the period keys
   */
  public static List<String> keys(String bucket, String from, String to) {
    validateBucket(bucket);
    LocalDate start = start(bucket, from);
    LocalDate endExclusive = endExclusive(bucket, to);
    return keys(bucket, start, endExclusive);
  }

  private static List<String> keys(String bucket, LocalDate start, LocalDate endExclusive) {
    List<String> keys = new ArrayList<>();
    switch (bucket) {
      case "daily" -> {
        for (LocalDate day = start; day.isBefore(endExclusive); day = day.plusDays(1)) {
          keys.add(day.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
      }
      case "weekly" -> {
        for (LocalDate week = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            week.isBefore(endExclusive);
            week = week.plusWeeks(1)) {
          keys.add(keyFor("weekly", week));
        }
      }
      case "monthly" -> {
        YearMonth month = YearMonth.from(start);
        YearMonth last = YearMonth.from(endExclusive.minusDays(1));
        while (!month.isAfter(last)) {
          keys.add(month.toString());
          month = month.plusMonths(1);
        }
      }
      case "yearly" -> {
        for (int year = start.getYear(); year <= endExclusive.minusDays(1).getYear(); year++) {
          keys.add(String.valueOf(year));
        }
      }
      default -> throw new InvalidAnalyticsPeriodException("Unsupported bucket: " + bucket);
    }
    return keys;
  }

  /**
   * Returns the bucket key for a concrete date.
   *
   * @param bucket the bucket name
   * @param date the date to locate
   * @return the bucket key (for example {@code 2026-11-21}, {@code 2026-W47}, {@code 2026-11} or
   *     {@code 2026})
   */
  public static String keyFor(String bucket, LocalDate date) {
    validateBucket(bucket);
    return switch (bucket) {
      case "daily" -> date.format(DateTimeFormatter.ISO_LOCAL_DATE);
      case "weekly" ->
          String.format(
              "%04d-W%02d",
              date.get(IsoFields.WEEK_BASED_YEAR), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
      case "monthly" -> YearMonth.from(date).toString();
      case "yearly" -> String.valueOf(date.getYear());
      default -> throw new InvalidAnalyticsPeriodException("Unsupported bucket: " + bucket);
    };
  }

  /**
   * Resolves the inclusive start date of a period key.
   *
   * @param bucket the bucket name
   * @param from the period key
   * @return the first calendar day covered by the key
   */
  public static LocalDate start(String bucket, String from) {
    try {
      return switch (bucket) {
        case "daily" -> LocalDate.parse(from, DateTimeFormatter.ISO_LOCAL_DATE);
        case "weekly" -> parseWeek(from).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        case "monthly" -> {
          validateMonthly(from);
          yield YearMonth.parse(from).atDay(1);
        }
        case "yearly" -> {
          validateYearly(from);
          yield LocalDate.of(Integer.parseInt(from), 1, 1);
        }
        default -> throw new InvalidAnalyticsPeriodException("Unsupported bucket: " + bucket);
      };
    } catch (DateTimeParseException | NumberFormatException exception) {
      throw invalidFormat(bucket, from, expectedFormat(bucket));
    }
  }

  /**
   * Resolves the exclusive end date of a period key (the day after the period ends).
   *
   * @param bucket the bucket name
   * @param to the period key
   * @return the exclusive end date
   */
  public static LocalDate endExclusive(String bucket, String to) {
    try {
      return switch (bucket) {
        case "daily" -> LocalDate.parse(to, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1);
        case "weekly" ->
            parseWeek(to).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(1);
        case "monthly" -> {
          validateMonthly(to);
          yield YearMonth.parse(to).plusMonths(1).atDay(1);
        }
        case "yearly" -> {
          validateYearly(to);
          yield LocalDate.of(Integer.parseInt(to) + 1, 1, 1);
        }
        default -> throw new InvalidAnalyticsPeriodException("Unsupported bucket: " + bucket);
      };
    } catch (DateTimeParseException | NumberFormatException exception) {
      throw invalidFormat(bucket, to, expectedFormat(bucket));
    }
  }

  /**
   * Returns the expected input format for a bucket.
   *
   * @param bucket the bucket name
   * @return the format description used in error messages
   */
  public static String expectedFormat(String bucket) {
    return switch (bucket) {
      case "daily" -> "YYYY-MM-DD";
      case "weekly" -> "YYYY-Www";
      case "monthly" -> "YYYY-MM";
      case "yearly" -> "YYYY";
      default -> "YYYY-MM-DD";
    };
  }

  private static LocalDate parseWeek(String key) {
    if (key == null || !WEEKLY.matcher(key).matches()) {
      throw invalidFormat("weekly", key, "YYYY-Www");
    }
    int year = Integer.parseInt(key.substring(0, 4));
    int week = Integer.parseInt(key.substring(6));
    if (week < 1 || week > 53) {
      throw invalidFormat("weekly", key, "YYYY-Www");
    }
    // ISO week 1 is the week containing January 4th.
    return LocalDate.of(year, 1, 4)
        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
  }

  private static void validateMonthly(String key) {
    if (key == null || !MONTHLY.matcher(key).matches()) {
      throw invalidFormat("monthly", key, "YYYY-MM");
    }
    int month = Integer.parseInt(key.substring(5));
    if (month < 1 || month > 12) {
      throw invalidFormat("monthly", key, "YYYY-MM");
    }
    YearMonth.parse(key);
  }

  private static void validateYearly(String key) {
    if (key == null || !YEARLY.matcher(key).matches()) {
      throw invalidFormat("yearly", key, "YYYY");
    }
  }

  private static InvalidAnalyticsPeriodException invalidFormat(
      String bucket, String value, String expected) {
    return new InvalidAnalyticsPeriodException(
        "Invalid period format for bucket '"
            + bucket
            + "': '"
            + value
            + "'. Expected: "
            + expected);
  }
}
