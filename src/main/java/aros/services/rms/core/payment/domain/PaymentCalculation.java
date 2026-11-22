/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;

/**
 * Validated money breakdown of a payment.
 *
 * @param subtotal server-recomputed order subtotal
 * @param discount the applied discount, or {@code null}
 * @param tip the tip amount
 * @param total {@code subtotal - discount + tip}
 * @param tendered sum of all tenders
 * @param change {@code tendered - total}, always at least zero
 */
public record PaymentCalculation(
    Money subtotal,
    PaymentDiscount discount,
    Money tip,
    Money total,
    Money tendered,
    Money change) {}
