/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import java.util.Currency;
import java.util.List;

/**
 * Stateless validator and calculator for the payment invariants.
 *
 * <p>The client never sends a subtotal or a total. The server computes both from the order and the
 * entered tenders, using {@link Money} for every operation. Percentage discounts are expressed in
 * basis points so the domain never needs a raw decimal.
 */
public final class PaymentCalculator {

  private static final int MAX_BASIS_POINTS = 10000;
  private static final int MAX_REASON_LENGTH = 255;

  private PaymentCalculator() {}

  /**
   * Validates the payment inputs and computes the money breakdown.
   *
   * <p>Invariants enforced:
   *
   * <ul>
   *   <li>{@code total = subtotal - discount + tip}
   *   <li>{@code 0 <= discount < subtotal}
   *   <li>{@code tip >= 0}
   *   <li>at least one positive tender
   *   <li>non-cash tenders never exceed the remaining amount
   *   <li>only the last cash tender may overpay, producing change
   *   <li>{@code change = tendered - total >= 0}
   *   <li>every amount uses the subtotal currency
   * </ul>
   *
   * @param subtotal the server-recomputed order subtotal
   * @param discountType the discount kind, or {@code null} for no discount
   * @param percentageBasisPoints the percentage in basis points for a percent discount
   * @param discountAmount the monetary amount for an amount discount
   * @param discountReason the discount justification, mandatory when a discount is present
   * @param tip the tip, or {@code null} for zero
   * @param tenders the tenders entered by the cashier
   * @return the validated money breakdown
   * @throws InvalidPaymentException when any invariant is violated
   */
  public static PaymentCalculation calculate(
      Money subtotal,
      DiscountType discountType,
      int percentageBasisPoints,
      Money discountAmount,
      String discountReason,
      Money tip,
      List<PaymentTenderInput> tenders) {

    if (subtotal == null || !subtotal.isPositive()) {
      throw new InvalidPaymentException("The order has no amount to charge");
    }
    if (tenders == null || tenders.isEmpty()) {
      throw new InvalidPaymentException("At least one tender is required");
    }

    PaymentDiscount discount =
        resolveDiscount(
            subtotal, discountType, percentageBasisPoints, discountAmount, discountReason);
    Money discountMoney = discount != null ? discount.amount() : Money.zero(subtotal.currency());
    Money resolvedTip =
        tip == null ? Money.zero(subtotal.currency()) : requireSameCurrency(tip, subtotal, "Tip");
    if (resolvedTip.isNegative()) {
      throw new InvalidPaymentException("The tip cannot be negative");
    }

    Money total = subtotal.minus(discountMoney).plus(resolvedTip);
    if (!total.isPositive()) {
      throw new InvalidPaymentException("The payment total must be positive");
    }

    Money tendered = Money.zero(subtotal.currency());
    Money remaining = total;
    for (int i = 0; i < tenders.size(); i++) {
      PaymentTenderInput tender = tenders.get(i);
      Money amount = requireSameCurrency(tender.amount(), subtotal, "Tender");
      if (!amount.isPositive()) {
        throw new InvalidPaymentException("Every tender amount must be positive");
      }
      if (amount.isGreaterThan(remaining)) {
        if (tender.method() != PaymentTenderMethod.CASH) {
          throw new InvalidPaymentException("Only a cash tender may exceed the remaining amount");
        }
        if (i != tenders.size() - 1) {
          throw new InvalidPaymentException(
              "Only the last cash tender may exceed the remaining amount");
        }
      }
      remaining = remaining.minus(amount);
      tendered = tendered.plus(amount);
    }

    if (remaining.isPositive()) {
      throw new InvalidPaymentException(
          "The tenders do not cover the total; " + remaining + " is still missing");
    }

    Money change = tendered.minus(total);
    return new PaymentCalculation(subtotal, discount, resolvedTip, total, tendered, change);
  }

  private static PaymentDiscount resolveDiscount(
      Money subtotal,
      DiscountType type,
      int percentageBasisPoints,
      Money discountAmount,
      String reason) {
    if (type == null) {
      if (percentageBasisPoints != 0 || discountAmount != null) {
        throw new InvalidPaymentException("A discount requires both a type and a value");
      }
      return null;
    }
    if (reason == null || reason.isBlank()) {
      throw new InvalidPaymentException("A discount reason is required");
    }
    if (reason.length() > MAX_REASON_LENGTH) {
      throw new InvalidPaymentException(
          "The discount reason must not exceed " + MAX_REASON_LENGTH + " characters");
    }

    if (type == DiscountType.PERCENT) {
      if (discountAmount != null) {
        throw new InvalidPaymentException("A percentage discount must not carry a money amount");
      }
      if (percentageBasisPoints <= 0) {
        throw new InvalidPaymentException("The discount percentage must be positive");
      }
      if (percentageBasisPoints > MAX_BASIS_POINTS) {
        throw new InvalidPaymentException("A percentage discount cannot exceed 100");
      }
      Money amount = subtotal.percentBasisPoints(percentageBasisPoints);
      if (!amount.isLessThan(subtotal)) {
        throw new InvalidPaymentException("The discount must be lower than the subtotal");
      }
      return new PaymentDiscount(type, percentageBasisPoints, amount, reason.trim());
    }

    if (percentageBasisPoints != 0) {
      throw new InvalidPaymentException("An amount discount must not carry a percentage");
    }
    if (discountAmount == null || !discountAmount.isPositive()) {
      throw new InvalidPaymentException("The discount value must be positive");
    }
    Money amount = requireSameCurrency(discountAmount, subtotal, "Discount");
    if (!amount.isLessThan(subtotal)) {
      throw new InvalidPaymentException("The discount must be lower than the subtotal");
    }
    return new PaymentDiscount(type, 0, amount, reason.trim());
  }

  private static Money requireSameCurrency(Money money, Money reference, String label) {
    Currency expected = reference.currency();
    if (money == null || !expected.equals(money.currency())) {
      throw new InvalidPaymentException(label + " currency must match the order currency");
    }
    return money;
  }
}
