/* (C) 2026 */

package aros.services.rms.core.cashshift.domain;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTender;
import java.util.List;

/**
 * Aggregated cash-shift totals derived from payments and manual movements.
 *
 * <p>{@code expectedCash = openingFloat + cashTendered - changeGiven + paidIn - paidOut}. Card and
 * transfer totals are informational and do not affect the drawer.
 *
 * @param paymentsCount number of completed payments
 * @param voidedCount number of voided payments
 * @param subtotal sum of completed subtotals
 * @param discounts sum of completed discounts
 * @param tips sum of completed tips
 * @param totalCharged sum of completed totals (net of discounts and including tips)
 * @param cash sum of cash tenders
 * @param card sum of card tenders
 * @param transfer sum of transfer tenders
 * @param changeGiven sum of change returned
 * @param paidIn sum of PAID_IN movements
 * @param paidOut sum of PAID_OUT movements
 * @param expectedCash cash that should be in the drawer
 */
public record CashShiftTotals(
    long paymentsCount,
    long voidedCount,
    Money subtotal,
    Money discounts,
    Money tips,
    Money totalCharged,
    Money cash,
    Money card,
    Money transfer,
    Money changeGiven,
    Money paidIn,
    Money paidOut,
    Money expectedCash) {

  /**
   * Computes the totals for a shift.
   *
   * @param payments every payment belonging to the shift, voided included
   * @param movements every manual movement belonging to the shift
   * @param openingFloat the float the shift was opened with
   * @return the aggregated totals
   */
  public static CashShiftTotals from(
      List<Payment> payments, List<CashMovement> movements, Money openingFloat) {
    Money zero = Money.zero(openingFloat.currency());
    long completed = 0;
    long voided = 0;
    Money subtotal = zero;
    Money discounts = zero;
    Money tips = zero;
    Money total = zero;
    Money cash = zero;
    Money card = zero;
    Money transfer = zero;
    Money change = zero;

    if (payments != null) {
      for (Payment payment : payments) {
        if (payment.getStatus() == PaymentStatus.VOIDED) {
          voided++;
          continue;
        }
        completed++;
        subtotal = subtotal.plus(payment.getSubtotal());
        if (payment.getDiscount() != null) {
          discounts = discounts.plus(payment.getDiscount().amount());
        }
        tips = tips.plus(payment.getTip());
        total = total.plus(payment.getTotal());
        change = change.plus(payment.getChange());
        if (payment.getTenders() != null) {
          for (PaymentTender tender : payment.getTenders()) {
            switch (tender.method()) {
              case CASH -> cash = cash.plus(tender.amount());
              case CARD -> card = card.plus(tender.amount());
              case TRANSFER -> transfer = transfer.plus(tender.amount());
              default ->
                  throw new IllegalStateException(
                      "Método de pago no soportado: " + tender.method());
            }
          }
        }
      }
    }

    Money paidIn = zero;
    Money paidOut = zero;
    if (movements != null) {
      for (CashMovement movement : movements) {
        switch (movement.type()) {
          case PAID_IN -> paidIn = paidIn.plus(movement.amount());
          case PAID_OUT -> paidOut = paidOut.plus(movement.amount());
          default ->
              throw new IllegalStateException(
                  "Tipo de movimiento de caja no soportado: " + movement.type());
        }
      }
    }

    Money expected = openingFloat.plus(cash).minus(change).plus(paidIn).minus(paidOut);

    return new CashShiftTotals(
        completed, voided, subtotal, discounts, tips, total, cash, card, transfer, change, paidIn,
        paidOut, expected);
  }
}
