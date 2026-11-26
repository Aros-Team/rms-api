/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.util.Objects;

/**
 * One manual discount applied to a payment.
 *
 * <p>{@code amount} is the resolved monetary value subtracted from the subtotal. A percentage is
 * kept as basis points (one basis point is 0.01%) and an amount discount keeps its money value; the
 * two representations never coexist.
 *
 * @param type the discount kind
 * @param percentageBasisPoints the percentage in basis points for {@link DiscountType#PERCENT},
 *     zero for {@link DiscountType#AMOUNT}
 * @param amount the resolved monetary discount
 * @param reason the mandatory human-readable justification
 */
public record PaymentDiscount(
    DiscountType type, int percentageBasisPoints, Money amount, String reason) {

  /**
   * Canonical constructor with validation.
   *
   * @param type the discount kind
   * @param percentageBasisPoints the percentage in basis points
   * @param amount the resolved monetary discount
   * @param reason the mandatory justification
   */
  public PaymentDiscount {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(reason, "reason");
    if (percentageBasisPoints < 0) {
      throw new IllegalArgumentException(
          "percentageBasisPoints no puede ser negativo, era: " + percentageBasisPoints);
    }
  }
}
