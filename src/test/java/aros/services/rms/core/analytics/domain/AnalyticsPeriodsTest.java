/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aros.services.rms.core.analytics.domain.exception.AnalyticsRangeTooLargeException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsPeriodException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsRangeException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AnalyticsPeriods}.
 *
 * <p><b>Feature:</b> Shared analytics period parsing, validation and key generation.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Daily, weekly, monthly and yearly keys are generated inclusively
 *   <li>A range resolves to a half-open date interval
 *   <li>Invalid formats, inverted ranges and oversized daily ranges are rejected
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Calls the static parser with boundary and ISO-week examples
 *   <li>Asserts the generated keys and resolved dates
 *   <li>Asserts the typed analytics exceptions for invalid input
 * </ul>
 */
class AnalyticsPeriodsTest {

  @Test
  void should_generate_daily_keys_inclusive() {
    List<String> keys = AnalyticsPeriods.validateAndGenerate("daily", "2026-11-20", "2026-11-22");
    assertEquals(List.of("2026-11-20", "2026-11-21", "2026-11-22"), keys);
  }

  @Test
  void should_generate_weekly_keys_across_iso_boundaries() {
    List<String> keys = AnalyticsPeriods.validateAndGenerate("weekly", "2026-W52", "2027-W02");
    assertEquals(List.of("2026-W52", "2026-W53", "2027-W01", "2027-W02"), keys);
  }

  @Test
  void should_generate_monthly_and_yearly_keys() {
    assertEquals(
        List.of("2026-11", "2026-12", "2027-01"),
        AnalyticsPeriods.validateAndGenerate("monthly", "2026-11", "2027-01"));
    assertEquals(
        List.of("2025", "2026"), AnalyticsPeriods.validateAndGenerate("yearly", "2025", "2026"));
  }

  @Test
  void should_resolve_half_open_date_range() {
    assertEquals(LocalDate.of(2026, 2, 1), AnalyticsPeriods.start("monthly", "2026-02"));
    assertEquals(LocalDate.of(2026, 3, 1), AnalyticsPeriods.endExclusive("monthly", "2026-02"));
  }

  @Test
  void should_reject_inverted_range() {
    assertThrows(
        InvalidAnalyticsRangeException.class,
        () -> AnalyticsPeriods.validateAndGenerate("monthly", "2026-07", "2026-06"));
  }

  @Test
  void should_reject_range_larger_than_maximum() {
    assertThrows(
        AnalyticsRangeTooLargeException.class,
        () -> AnalyticsPeriods.validateAndGenerate("daily", "2025-01-01", "2026-06-01"));
  }

  @Test
  void should_reject_invalid_format_and_bucket() {
    assertThrows(
        InvalidAnalyticsPeriodException.class,
        () -> AnalyticsPeriods.validateAndGenerate("monthly", "2026-13", "2026-14"));
    assertThrows(
        InvalidAnalyticsPeriodException.class,
        () -> AnalyticsPeriods.validateAndGenerate("quarterly", "2026-01", "2026-02"));
  }

  @Test
  void should_build_bucket_keys_for_date() {
    LocalDate date = LocalDate.of(2026, 11, 21);
    assertEquals("2026-11-21", AnalyticsPeriods.keyFor("daily", date));
    assertEquals("2026-11", AnalyticsPeriods.keyFor("monthly", date));
    assertEquals("2026", AnalyticsPeriods.keyFor("yearly", date));
    assertEquals("2026-W47", AnalyticsPeriods.keyFor("weekly", date));
  }
}
