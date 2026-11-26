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
      throw new InvalidPaymentException("El pedido no tiene un monto para cobrar");
    }
    if (tenders == null || tenders.isEmpty()) {
      throw new InvalidPaymentException("Se requiere al menos un medio de pago");
    }

    PaymentDiscount discount =
        resolveDiscount(
            subtotal, discountType, percentageBasisPoints, discountAmount, discountReason);
    Money discountMoney = discount != null ? discount.amount() : Money.zero(subtotal.currency());
    Money resolvedTip =
        tip == null
            ? Money.zero(subtotal.currency())
            : requireSameCurrency(tip, subtotal, "Propina");
    if (resolvedTip.isNegative()) {
      throw new InvalidPaymentException("La propina no puede ser negativa");
    }

    Money total = subtotal.minus(discountMoney).plus(resolvedTip);
    if (!total.isPositive()) {
      throw new InvalidPaymentException("El total del pago debe ser positivo");
    }

    Money tendered = Money.zero(subtotal.currency());
    Money remaining = total;
    for (int i = 0; i < tenders.size(); i++) {
      PaymentTenderInput tender = tenders.get(i);
      Money amount = requireSameCurrency(tender.amount(), subtotal, "Pago");
      if (!amount.isPositive()) {
        throw new InvalidPaymentException("Cada monto de pago debe ser positivo");
      }
      if (amount.isGreaterThan(remaining)) {
        if (tender.method() != PaymentTenderMethod.CASH) {
          throw new InvalidPaymentException(
              "Solo un pago en efectivo puede superar el monto restante");
        }
        if (i != tenders.size() - 1) {
          throw new InvalidPaymentException(
              "Solo el último pago en efectivo puede superar el monto restante");
        }
      }
      remaining = remaining.minus(amount);
      tendered = tendered.plus(amount);
    }

    if (remaining.isPositive()) {
      throw new InvalidPaymentException(
          "Los medios de pago no cubren el total; " + remaining + " aún falta");
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
      throw new InvalidPaymentException("El motivo del descuento es obligatorio");
    }
    if (reason.length() > MAX_REASON_LENGTH) {
      throw new InvalidPaymentException(
          "El motivo del descuento no debe superar " + MAX_REASON_LENGTH + " characters");
    }

    if (type == DiscountType.PERCENT) {
      if (discountAmount != null) {
        throw new InvalidPaymentException(
            "Un descuento porcentual no debe incluir un monto de dinero");
      }
      if (percentageBasisPoints <= 0) {
        throw new InvalidPaymentException("El porcentaje de descuento debe ser positivo");
      }
      if (percentageBasisPoints > MAX_BASIS_POINTS) {
        throw new InvalidPaymentException("El porcentaje de descuento no puede superar 100");
      }
      Money amount = subtotal.percentBasisPoints(percentageBasisPoints);
      if (!amount.isLessThan(subtotal)) {
        throw new InvalidPaymentException("El descuento debe ser menor que el subtotal");
      }
      return new PaymentDiscount(type, percentageBasisPoints, amount, reason.trim());
    }

    if (percentageBasisPoints != 0) {
      throw new InvalidPaymentException("Un descuento por monto no debe incluir un porcentaje");
    }
    if (discountAmount == null || !discountAmount.isPositive()) {
      throw new InvalidPaymentException("El valor del descuento debe ser positivo");
    }
    Money amount = requireSameCurrency(discountAmount, subtotal, "Descuento");
    if (!amount.isLessThan(subtotal)) {
      throw new InvalidPaymentException("El descuento debe ser menor que el subtotal");
    }
    return new PaymentDiscount(type, 0, amount, reason.trim());
  }

  private static Money requireSameCurrency(Money money, Money reference, String label) {
    Currency expected = reference.currency();
    if (money == null || !expected.equals(money.currency())) {
      throw new InvalidPaymentException(label + ": la moneda debe coincidir con la del pedido");
    }
    return money;
  }
}
