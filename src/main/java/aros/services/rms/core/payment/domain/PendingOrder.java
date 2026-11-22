/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDateTime;

/**
 * Read model for the cashier board: a delivered order that still has no active payment.
 *
 * @param orderId the order identifier
 * @param tableId the table identifier, may be {@code null}
 * @param tableNumber the table number, may be {@code null}
 * @param partySize the party size, may be {@code null}
 * @param deliveredAt the order date used as the business-date anchor
 * @param total the server-computed order total
 * @param paymentStatus {@link PendingPaymentStatus#UNPAID} or {@link PendingPaymentStatus#VOIDED}
 */
public record PendingOrder(
    Long orderId,
    Long tableId,
    Integer tableNumber,
    Integer partySize,
    LocalDateTime deliveredAt,
    Money total,
    PendingPaymentStatus paymentStatus) {}
