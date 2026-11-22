/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.domain.DiscountType;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentDiscount;
import aros.services.rms.core.payment.domain.PaymentTender;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.springframework.stereotype.Component;

/** Maps between the payment domain aggregate and its JPA entities. */
@Component
public class PaymentEntityMapper {

  /**
   * Converts a domain payment to a new entity graph.
   *
   * @param payment the domain payment
   * @return the entity ready to insert
   */
  public PaymentEntity toEntity(Payment payment) {
    if (payment == null) {
      return null;
    }
    Currency currency = payment.getSubtotal().currency();
    PaymentDiscount discount = payment.getDiscount();
    PaymentEntity entity =
        PaymentEntity.builder()
            .id(payment.getId())
            .orderId(payment.getOrderId())
            .shiftId(payment.getShiftId())
            .status(payment.getStatus())
            .currency(currency.getCurrencyCode())
            .subtotal(payment.getSubtotal().amount())
            .discountType(discount != null ? discount.type() : null)
            .discountValue(rawDiscountValue(discount))
            .discountAmount(
                discount != null ? discount.amount().amount() : BigDecimal.ZERO.setScale(2))
            .discountReason(discount != null ? discount.reason() : null)
            .tipAmount(payment.getTip().amount())
            .totalAmount(payment.getTotal().amount())
            .tenderedAmount(payment.getTendered().amount())
            .changeAmount(payment.getChange().amount())
            .receiptNumber(payment.getReceiptNumber())
            .cashierId(payment.getCashierId())
            .idempotencyKey(payment.getIdempotencyKey())
            .createdAt(payment.getCreatedAt())
            .completedAt(payment.getCompletedAt())
            .voidedAt(payment.getVoidedAt())
            .voidedBy(payment.getVoidedBy())
            .voidReason(payment.getVoidReason())
            .build();

    List<PaymentTenderEntity> tenders = new ArrayList<>();
    for (PaymentTender tender : payment.getTenders()) {
      tenders.add(
          PaymentTenderEntity.builder()
              .payment(entity)
              .method(tender.method())
              .amount(tender.amount().amount())
              .reference(tender.reference())
              .sortOrder(tender.sortOrder())
              .build());
    }
    entity.setTenders(tenders);
    return entity;
  }

  /**
   * Persists a percentage as its human-readable percentage value and an amount as its money value,
   * keeping the {@code discount_value} column semantics unchanged.
   */
  private static BigDecimal rawDiscountValue(PaymentDiscount discount) {
    if (discount == null) {
      return null;
    }
    if (discount.type() == DiscountType.PERCENT) {
      return BigDecimal.valueOf(discount.percentageBasisPoints()).movePointLeft(2);
    }
    return discount.amount().amount();
  }

  private static int basisPointsOf(PaymentEntity entity) {
    if (entity.getDiscountType() != DiscountType.PERCENT || entity.getDiscountValue() == null) {
      return 0;
    }
    return entity.getDiscountValue().movePointRight(2).intValue();
  }

  /**
   * Converts an entity graph to the domain aggregate.
   *
   * @param entity the entity
   * @return the domain payment
   */
  public Payment toDomain(PaymentEntity entity) {
    if (entity == null) {
      return null;
    }
    Currency currency = Currency.getInstance(entity.getCurrency());
    Money subtotal = new Money(entity.getSubtotal(), currency);
    Money tip = new Money(entity.getTipAmount(), currency);
    Money total = new Money(entity.getTotalAmount(), currency);
    PaymentDiscount discount = null;
    if (entity.getDiscountType() != null) {
      discount =
          new PaymentDiscount(
              entity.getDiscountType(),
              basisPointsOf(entity),
              new Money(entity.getDiscountAmount(), currency),
              entity.getDiscountReason());
    }

    List<PaymentTender> tenders = new ArrayList<>();
    if (entity.getTenders() != null) {
      for (PaymentTenderEntity tender : entity.getTenders()) {
        tenders.add(
            new PaymentTender(
                tender.getId(),
                tender.getMethod(),
                new Money(tender.getAmount(), currency),
                tender.getReference(),
                tender.getSortOrder()));
      }
    }

    return Payment.builder()
        .id(entity.getId())
        .orderId(entity.getOrderId())
        .shiftId(entity.getShiftId())
        .status(entity.getStatus())
        .receiptNumber(entity.getReceiptNumber())
        .subtotal(subtotal)
        .discount(discount)
        .tip(tip)
        .total(total)
        .tendered(new Money(entity.getTenderedAmount(), currency))
        .change(new Money(entity.getChangeAmount(), currency))
        .tenders(tenders)
        .cashierId(entity.getCashierId())
        .idempotencyKey(entity.getIdempotencyKey())
        .createdAt(entity.getCreatedAt())
        .completedAt(entity.getCompletedAt())
        .voidedAt(entity.getVoidedAt())
        .voidedBy(entity.getVoidedBy())
        .voidReason(entity.getVoidReason())
        .version(entity.getVersion())
        .build();
  }
}
