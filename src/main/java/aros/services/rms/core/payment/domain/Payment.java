/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment aggregate linked to a delivered order.
 *
 * <p>The order status is never changed: the cashier state is derived from {@link PaymentStatus}.
 * Money fields are an immutable snapshot taken by the server at charge time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
  private Long id;
  private Long orderId;
  private Long shiftId;
  @Builder.Default private PaymentStatus status = PaymentStatus.COMPLETED;
  private String receiptNumber;
  private Money subtotal;
  private PaymentDiscount discount;
  private Money tip;
  private Money total;
  private Money tendered;
  private Money change;
  @Builder.Default private List<PaymentTender> tenders = List.of();
  private Long cashierId;
  private String idempotencyKey;
  private LocalDateTime createdAt;
  private LocalDateTime completedAt;
  private LocalDateTime voidedAt;
  private Long voidedBy;
  private String voidReason;
  private Long version;

  /**
   * Returns the resolved discount amount, or zero in the payment currency when there is none.
   *
   * @return the discount amount
   */
  public Money discountAmount() {
    return discount != null ? discount.amount() : Money.zero(total.currency());
  }
}
