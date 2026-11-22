/* (C) 2026 */

package aros.services.rms.core.payment.domain;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.OrderStatus;
import java.time.LocalDateTime;

/**
 * Minimal order snapshot needed to charge safely.
 *
 * <p>It is loaded with a pessimistic row lock so two terminals cannot charge the same order twice.
 *
 * @param id the order identifier
 * @param status the current order status
 * @param subtotal the server-computed sum of line unit prices
 * @param tableId the table identifier, may be {@code null}
 * @param tableNumber the table number, may be {@code null}
 * @param partySize the party size, may be {@code null}
 * @param date the order business date
 */
public record PayableOrder(
    Long id,
    OrderStatus status,
    Money subtotal,
    Long tableId,
    Integer tableNumber,
    Integer partySize,
    LocalDateTime date) {}
