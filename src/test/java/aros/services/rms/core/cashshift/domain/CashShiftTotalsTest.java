/* (C) 2026 */

package aros.services.rms.core.cashshift.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTender;
import aros.services.rms.core.payment.domain.PaymentTenderMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CashShiftTotals}.
 *
 * <p><b>Feature:</b> Z-summary math derived from payments and manual cash movements.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Voided payments are excluded from money totals but counted
 *   <li>Expected cash includes opening float, cash tenders, change, paid-in and paid-out
 *   <li>Card and transfer are reported but do not affect expected cash
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds payments with mixed tenders plus a voided row
 *   <li>Asserts every aggregate against hand-computed values
 * </ul>
 */
class CashShiftTotalsTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Test
  void shouldComputeExpectedCashFromPaymentsAndMovements() {
    List<Payment> payments =
        List.of(
            payment(
                PaymentStatus.COMPLETED,
                "58000.00",
                "5000.00",
                "63000.00",
                "7000.00",
                List.of(tender(PaymentTenderMethod.CASH, "70000.00", 0))),
            payment(
                PaymentStatus.COMPLETED,
                "50000.00",
                "0.00",
                "50000.00",
                "0.00",
                List.of(tender(PaymentTenderMethod.CARD, "50000.00", 0))),
            payment(
                PaymentStatus.VOIDED,
                "99999.00",
                "0.00",
                "99999.00",
                "0.00",
                List.of(tender(PaymentTenderMethod.CASH, "99999.00", 0))));

    List<CashMovement> movements =
        List.of(
            movement(CashMovementType.PAID_IN, "20000.00"),
            movement(CashMovementType.PAID_OUT, "5000.00"));

    CashShiftTotals totals = CashShiftTotals.from(payments, movements, money("100000.00"));

    assertEquals(2, totals.paymentsCount());
    assertEquals(1, totals.voidedCount());
    assertEquals(new BigDecimal("108000.00"), totals.subtotal().amount());
    assertEquals(new BigDecimal("113000.00"), totals.totalCharged().amount());
    assertEquals(new BigDecimal("5000.00"), totals.tips().amount());
    assertEquals(new BigDecimal("70000.00"), totals.cash().amount());
    assertEquals(new BigDecimal("50000.00"), totals.card().amount());
    assertEquals(new BigDecimal("0.00"), totals.transfer().amount());
    assertEquals(new BigDecimal("7000.00"), totals.changeGiven().amount());
    assertEquals(new BigDecimal("20000.00"), totals.paidIn().amount());
    assertEquals(new BigDecimal("5000.00"), totals.paidOut().amount());
    assertEquals(new BigDecimal("178000.00"), totals.expectedCash().amount());
  }

  @Test
  void shouldReportCardAndTransferWithoutTouchingTheDrawer() {
    List<Payment> payments =
        List.of(
            payment(
                PaymentStatus.COMPLETED,
                "10000.00",
                "0.00",
                "10000.00",
                "0.00",
                List.of(
                    tender(PaymentTenderMethod.TRANSFER, "6000.00", 0),
                    tender(PaymentTenderMethod.CARD, "4000.00", 1))));

    CashShiftTotals totals = CashShiftTotals.from(payments, List.of(), money("50000.00"));

    assertEquals(new BigDecimal("6000.00"), totals.transfer().amount());
    assertEquals(new BigDecimal("4000.00"), totals.card().amount());
    assertEquals(new BigDecimal("0.00"), totals.cash().amount());
    assertEquals(new BigDecimal("50000.00"), totals.expectedCash().amount());
  }

  private static Payment payment(
      PaymentStatus status,
      String subtotal,
      String tip,
      String total,
      String change,
      List<PaymentTender> tenders) {
    return Payment.builder()
        .id(1L)
        .orderId(10L)
        .shiftId(12L)
        .status(status)
        .subtotal(money(subtotal))
        .tip(money(tip))
        .total(money(total))
        .tendered(money(total))
        .change(money(change))
        .tenders(tenders)
        .build();
  }

  private static PaymentTender tender(PaymentTenderMethod method, String amount, int order) {
    return new PaymentTender(null, method, money(amount), null, order);
  }

  private static CashMovement movement(CashMovementType type, String amount) {
    return new CashMovement(
        1L, 12L, type, money(amount), "Test movement", 1001L, LocalDateTime.now());
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
