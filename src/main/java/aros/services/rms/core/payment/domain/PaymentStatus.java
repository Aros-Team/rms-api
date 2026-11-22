/* (C) 2026 */

package aros.services.rms.core.payment.domain;

/** Lifecycle of a payment. A completed payment is immutable except for a full void. */
public enum PaymentStatus {
  COMPLETED,
  VOIDED
}
