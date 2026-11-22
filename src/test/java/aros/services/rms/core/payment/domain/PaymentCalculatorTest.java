/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PaymentCalculator}.
 *
 * <p><b>Feature:</b> Server-side payment math: totals, discount, tip, split tenders and change.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>{@code total = subtotal - discount + tip}
 *   <li>Split tenders must be covered exactly; only the last cash tender may overpay
 *   <li>{@code change = tendered - total}
 *   <li>Invalid discounts, tips and tenders are rejected
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Calls the pure calculator with COP money and asserts the returned breakdown
 *   <li>Asserts an exception for every rejected invariant
 * </ul>
 */
class PaymentCalculatorTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Test
  void shouldComputeTotalWithTipAndCashChange() {
    PaymentCalculation result =
        PaymentCalculator.calculate(
            money("58000.00"), null, 0, null, null, money("5000.00"), List.of(cash("70000.00")));

    assertEquals(new BigDecimal("58000.00"), result.subtotal().amount());
    assertEquals(new BigDecimal("63000.00"), result.total().amount());
    assertEquals(new BigDecimal("70000.00"), result.tendered().amount());
    assertEquals(new BigDecimal("7000.00"), result.change().amount());
  }

  @Test
  void shouldSplitCardAndCashWithoutChange() {
    PaymentCalculation result =
        PaymentCalculator.calculate(
            money("58000.00"),
            null,
            0,
            null,
            null,
            null,
            List.of(card("50000.00", "AUTH-1"), cash("8000.00")));

    assertEquals(new BigDecimal("58000.00"), result.tendered().amount());
    assertEquals(new BigDecimal("0.00"), result.change().amount());
  }

  @Test
  void shouldApplyPercentageDiscountBeforeTip() {
    PaymentCalculation result =
        PaymentCalculator.calculate(
            money("58000.00"),
            DiscountType.PERCENT,
            1000,
            null,
            "Cortesia gerencia",
            null,
            List.of(cash("52200.00")));

    assertEquals(new BigDecimal("5800.00"), result.discount().amount().amount());
    assertEquals(1000, result.discount().percentageBasisPoints());
    assertEquals(new BigDecimal("52200.00"), result.total().amount());
  }

  @Test
  void shouldApplyAmountDiscount() {
    PaymentCalculation result =
        PaymentCalculator.calculate(
            money("58000.00"),
            DiscountType.AMOUNT,
            0,
            money("8000.00"),
            "Descuento",
            money("1000.00"),
            List.of(cash("51000.00")));

    assertEquals(new BigDecimal("8000.00"), result.discount().amount().amount());
    assertEquals(new BigDecimal("51000.00"), result.total().amount());
  }

  @Test
  void shouldRejectDiscountEqualToSubtotal() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"),
                DiscountType.AMOUNT,
                0,
                money("58000.00"),
                "Cortesia",
                null,
                List.of(cash("1.00"))));
  }

  @Test
  void shouldRequireDiscountReason() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"),
                DiscountType.PERCENT,
                1000,
                null,
                "  ",
                null,
                List.of(cash("52200.00"))));
  }

  @Test
  void shouldRejectNegativeTip() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"), null, 0, null, null, money("-1.00"), List.of(cash("58000.00"))));
  }

  @Test
  void shouldRejectNonCashTenderAboveRemaining() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"), null, 0, null, null, null, List.of(card("60000.00", "AUTH-1"))));
  }

  @Test
  void shouldRejectCashOverpayWhenItIsNotTheLastTender() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"),
                null,
                0,
                null,
                null,
                null,
                List.of(cash("60000.00"), card("1000.00", "AUTH-1"))));
  }

  @Test
  void shouldRejectUnderpayment() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"), null, 0, null, null, null, List.of(cash("50000.00"))));
  }

  @Test
  void shouldRejectZeroTenderAmount() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"), null, 0, null, null, null, List.of(cash("0.00"))));
  }

  @Test
  void shouldRejectPercentageAboveOneHundred() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"),
                DiscountType.PERCENT,
                10001,
                null,
                "Cortesia",
                null,
                List.of(cash("1.00"))));
  }

  @Test
  void shouldRejectPercentageDiscountWithMoneyAmount() {
    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"),
                DiscountType.PERCENT,
                1000,
                money("5800.00"),
                "Cortesia",
                null,
                List.of(cash("52200.00"))));
  }

  @Test
  void shouldRejectTenderInAnotherCurrency() {
    Currency usd = Currency.getInstance("USD");
    PaymentTenderInput foreign =
        new PaymentTenderInput(
            PaymentTenderMethod.CASH, new Money(new BigDecimal("58000.00"), usd), null);

    assertThrows(
        InvalidPaymentException.class,
        () ->
            PaymentCalculator.calculate(
                money("58000.00"), null, 0, null, null, null, List.of(foreign)));
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }

  private static PaymentTenderInput cash(String amount) {
    return new PaymentTenderInput(PaymentTenderMethod.CASH, money(amount), null);
  }

  private static PaymentTenderInput card(String amount, String reference) {
    return new PaymentTenderInput(PaymentTenderMethod.CARD, money(amount), reference);
  }
}
