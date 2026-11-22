/* (C) 2026 */

package aros.services.rms.core.cashshift.domain;

/** Lifecycle of a cash shift (drawer session). A closed shift is immutable in v1. */
public enum CashShiftStatus {
  OPEN,
  CLOSED
}
