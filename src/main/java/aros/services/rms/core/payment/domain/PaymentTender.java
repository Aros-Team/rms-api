/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.util.Objects;

/**
 * One tender of a (possibly split) payment.
 *
 * @param id persistence identifier, {@code null} before saving
 * @param method the tender method
 * @param amount the tendered amount
 * @param reference optional free-text reference for card or transfer
 * @param sortOrder the order in which the cashier entered the tender
 */
public record PaymentTender(
    Long id, PaymentTenderMethod method, Money amount, String reference, int sortOrder) {

  /**
   * Canonical constructor with validation.
   *
   * @param id persistence identifier
   * @param method the tender method
   * @param amount the tendered amount
   * @param reference optional reference
   * @param sortOrder entry order
   */
  public PaymentTender {
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(amount, "amount");
  }
}
