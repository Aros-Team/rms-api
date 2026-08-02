/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

/** Represents the lifecycle status of a payroll record. */
public enum PayrollStatus {
  PENDING,
  PAID,
  ACCRUED;

  /**
   * Checks if a transition to the target status is allowed.
   *
   * <p>Allowed transitions:
   *
   * <ul>
   *   <li>PENDING → PAID or ACCRUED
   *   <li>ACCRUED → PAID
   *   <li>PAID → nothing (immutable)
   * </ul>
   *
   * @param target the desired target status
   * @return true if the transition is valid
   */
  public boolean canTransitionTo(PayrollStatus target) {
    return switch (this) {
      case PENDING -> target == PAID || target == ACCRUED;
      case ACCRUED -> target == PAID;
      case PAID -> false;
    };
  }
}
