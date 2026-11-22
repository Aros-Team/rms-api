/* (C) 2026 */

package aros.services.rms.core.payment.domain;

/** Supported tender methods in v1. No card gateway or terminal integration exists. */
public enum PaymentTenderMethod {
  CASH,
  CARD,
  TRANSFER
}
