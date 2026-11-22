/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.util.Objects;

/**
 * Raw tender entered by the cashier, before it becomes a {@link PaymentTender}.
 *
 * @param method the tender method
 * @param amount the amount entered, expressed as money
 * @param reference optional card or transfer reference
 */
public record PaymentTenderInput(PaymentTenderMethod method, Money amount, String reference) {

  /**
   * Canonical constructor with validation.
   *
   * @param method the tender method
   * @param amount the amount entered
   * @param reference optional reference
   */
  public PaymentTenderInput {
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(amount, "amount");
  }
}
