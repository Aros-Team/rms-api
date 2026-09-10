/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import java.math.BigDecimal;

/** Types of payroll events that can be registered for a user. */
public enum PayrollEventType {
  OVERTIME,
  NIGHT_SURCHARGE,
  ABSENCE,
  BONUS_ATTENDANCE,
  BONUS_PERFORMANCE,
  DEDUCTION;

  /**
   * Returns whether this event type is a bonus.
   *
   * @return true if BONUS_ATTENDANCE or BONUS_PERFORMANCE
   */
  public boolean isBonus() {
    return this == BONUS_ATTENDANCE || this == BONUS_PERFORMANCE;
  }

  /**
   * Returns whether this event type is a deduction.
   *
   * @return true if ABSENCE or DEDUCTION
   */
  public boolean isDeduction() {
    return this == ABSENCE || this == DEDUCTION;
  }

  /**
   * Returns whether this event type is hours-based (quantity represents hours).
   *
   * @return true if OVERTIME or NIGHT_SURCHARGE
   */
  public boolean isHoursBased() {
    return this == OVERTIME || this == NIGHT_SURCHARGE;
  }

  /**
   * Returns the multiplier for this event type applied to the user's hourly rate.
   *
   * @return the multiplier, or null if this type requires a manual rate (BONUS_PERFORMANCE,
   *     DEDUCTION)
   */
  public BigDecimal getMultiplier() {
    return switch (this) {
      case OVERTIME -> new BigDecimal("1.5");
      case NIGHT_SURCHARGE -> new BigDecimal("1.75");
      case ABSENCE -> new BigDecimal("-1");
      case BONUS_ATTENDANCE -> new BigDecimal("0.5");
      case BONUS_PERFORMANCE, DEDUCTION -> null;
    };
  }

  /**
   * Returns whether this event type has an automatic multiplier.
   *
   * @return true if the type has a multiplier (not manual)
   */
  public boolean hasAutomaticMultiplier() {
    return getMultiplier() != null;
  }
}
