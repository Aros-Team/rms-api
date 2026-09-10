/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PayrollEventType} enum methods. */
class PayrollEventTypeTest {

  // ---------------------------------------------------------------------------
  // UC-01: should_identify_bonus_types
  // ---------------------------------------------------------------------------

  @Test
  void should_identify_bonus_types() {
    assertTrue(PayrollEventType.BONUS_ATTENDANCE.isBonus());
    assertTrue(PayrollEventType.BONUS_PERFORMANCE.isBonus());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_not_identify_non_bonus_as_bonus
  // ---------------------------------------------------------------------------

  @Test
  void should_not_identify_non_bonus_as_bonus() {
    assertFalse(PayrollEventType.OVERTIME.isBonus());
    assertFalse(PayrollEventType.NIGHT_SURCHARGE.isBonus());
    assertFalse(PayrollEventType.ABSENCE.isBonus());
    assertFalse(PayrollEventType.DEDUCTION.isBonus());
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_identify_deduction_types
  // ---------------------------------------------------------------------------

  @Test
  void should_identify_deduction_types() {
    assertTrue(PayrollEventType.ABSENCE.isDeduction());
    assertTrue(PayrollEventType.DEDUCTION.isDeduction());
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_not_identify_non_deduction_as_deduction
  // ---------------------------------------------------------------------------

  @Test
  void should_not_identify_non_deduction_as_deduction() {
    assertFalse(PayrollEventType.OVERTIME.isDeduction());
    assertFalse(PayrollEventType.NIGHT_SURCHARGE.isDeduction());
    assertFalse(PayrollEventType.BONUS_ATTENDANCE.isDeduction());
    assertFalse(PayrollEventType.BONUS_PERFORMANCE.isDeduction());
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_identify_hours_based_types
  // ---------------------------------------------------------------------------

  @Test
  void should_identify_hours_based_types() {
    assertTrue(PayrollEventType.OVERTIME.isHoursBased());
    assertTrue(PayrollEventType.NIGHT_SURCHARGE.isHoursBased());
  }

  // ---------------------------------------------------------------------------
  // UC-06: should_not_identify_non_hours_based
  // ---------------------------------------------------------------------------

  @Test
  void should_not_identify_non_hours_based() {
    assertFalse(PayrollEventType.ABSENCE.isHoursBased());
    assertFalse(PayrollEventType.BONUS_ATTENDANCE.isHoursBased());
    assertFalse(PayrollEventType.BONUS_PERFORMANCE.isHoursBased());
    assertFalse(PayrollEventType.DEDUCTION.isHoursBased());
  }

  // ---------------------------------------------------------------------------
  // UC-07: should_return_correct_multipliers
  // ---------------------------------------------------------------------------

  @Test
  void should_return_correct_multipliers() {
    assertEquals(0, new BigDecimal("1.5").compareTo(PayrollEventType.OVERTIME.getMultiplier()));
    assertEquals(
        0, new BigDecimal("1.75").compareTo(PayrollEventType.NIGHT_SURCHARGE.getMultiplier()));
    assertEquals(0, new BigDecimal("-1").compareTo(PayrollEventType.ABSENCE.getMultiplier()));
    assertEquals(
        0, new BigDecimal("0.5").compareTo(PayrollEventType.BONUS_ATTENDANCE.getMultiplier()));
  }

  // ---------------------------------------------------------------------------
  // UC-08: should_return_null_multiplier_for_manual_types
  // ---------------------------------------------------------------------------

  @Test
  void should_return_null_multiplier_for_manual_types() {
    assertNull(PayrollEventType.BONUS_PERFORMANCE.getMultiplier());
    assertNull(PayrollEventType.DEDUCTION.getMultiplier());
  }

  // ---------------------------------------------------------------------------
  // UC-09: should_identify_automatic_multiplier_types
  // ---------------------------------------------------------------------------

  @Test
  void should_identify_automatic_multiplier_types() {
    assertTrue(PayrollEventType.OVERTIME.hasAutomaticMultiplier());
    assertTrue(PayrollEventType.NIGHT_SURCHARGE.hasAutomaticMultiplier());
    assertTrue(PayrollEventType.ABSENCE.hasAutomaticMultiplier());
    assertTrue(PayrollEventType.BONUS_ATTENDANCE.hasAutomaticMultiplier());
  }

  // ---------------------------------------------------------------------------
  // UC-10: should_not_have_automatic_multiplier_for_manual_types
  // ---------------------------------------------------------------------------

  @Test
  void should_not_have_automatic_multiplier_for_manual_types() {
    assertFalse(PayrollEventType.BONUS_PERFORMANCE.hasAutomaticMultiplier());
    assertFalse(PayrollEventType.DEDUCTION.hasAutomaticMultiplier());
  }

  // ---------------------------------------------------------------------------
  // UC-11: should_have_six_values
  // ---------------------------------------------------------------------------

  @Test
  void should_have_six_values() {
    assertEquals(6, PayrollEventType.values().length);
  }
}
