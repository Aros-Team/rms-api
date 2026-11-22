/* (C) 2026 */

package aros.services.rms.core.payment.application.dto;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.domain.DiscountType;
import aros.services.rms.core.payment.domain.PaymentTenderInput;
import java.util.List;

/**
 * Command to charge a delivered order.
 *
 * <p>The subtotal and total are not part of the command: the server derives them from the order.
 * Every monetary input is already expressed as {@link Money}; percentages use basis points.
 *
 * @param orderId the delivered order to charge
 * @param tipAmount the tip, or {@code null} for zero
 * @param discount optional manual discount
 * @param tenders the tenders entered by the cashier
 * @param idempotencyKey the client-generated key for this charge attempt
 * @param cashierId the authenticated cashier
 */
public record RegisterPaymentCommand(
    Long orderId,
    Money tipAmount,
    DiscountCommand discount,
    List<PaymentTenderInput> tenders,
    String idempotencyKey,
    Long cashierId) {

  /**
   * One manual discount.
   *
   * @param type the discount kind
   * @param percentageBasisPoints the percentage in basis points for a percent discount
   * @param amount the monetary amount for an amount discount
   * @param reason the mandatory justification
   */
  public record DiscountCommand(
      DiscountType type, int percentageBasisPoints, Money amount, String reason) {}
}
