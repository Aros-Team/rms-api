/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link PayrollStatus} transition rules. */
class PayrollStatusTest {

  // ---------------------------------------------------------------------------
  // PENDING transitions
  // ---------------------------------------------------------------------------

  @Test
  void pendingCanTransitionToPaid() {
    assertTrue(PayrollStatus.PENDING.canTransitionTo(PayrollStatus.PAID));
  }

  @Test
  void pendingCanTransitionToAccrued() {
    assertTrue(PayrollStatus.PENDING.canTransitionTo(PayrollStatus.ACCRUED));
  }

  @Test
  void pendingCannotTransitionToPending() {
    assertFalse(PayrollStatus.PENDING.canTransitionTo(PayrollStatus.PENDING));
  }

  // ---------------------------------------------------------------------------
  // ACCRUED transitions
  // ---------------------------------------------------------------------------

  @Test
  void accruedCanTransitionToPaid() {
    assertTrue(PayrollStatus.ACCRUED.canTransitionTo(PayrollStatus.PAID));
  }

  @Test
  void accruedCannotTransitionToPending() {
    assertFalse(PayrollStatus.ACCRUED.canTransitionTo(PayrollStatus.PENDING));
  }

  @Test
  void accruedCannotTransitionToAccrued() {
    assertFalse(PayrollStatus.ACCRUED.canTransitionTo(PayrollStatus.ACCRUED));
  }

  // ---------------------------------------------------------------------------
  // PAID transitions (terminal)
  // ---------------------------------------------------------------------------

  @Test
  void paidIsTerminal() {
    assertFalse(PayrollStatus.PAID.canTransitionTo(PayrollStatus.PENDING));
    assertFalse(PayrollStatus.PAID.canTransitionTo(PayrollStatus.ACCRUED));
    assertFalse(PayrollStatus.PAID.canTransitionTo(PayrollStatus.PAID));
  }
}
