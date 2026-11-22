/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api.dto;

import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Response DTO for the cashier pending-orders board. */
@Schema(description = "Delivered order pending payment")
public record PendingOrderResponse(
    @Schema(description = "Order ID") Long orderId,
    @Schema(description = "Table ID") Long tableId,
    @Schema(description = "Table number") Integer tableNumber,
    @Schema(description = "Party size") Integer partySize,
    @Schema(description = "Order business timestamp") LocalDateTime deliveredAt,
    @Schema(description = "Server-computed order total") MoneyDto total,
    @Schema(description = "UNPAID, or VOIDED when a previous charge was voided")
        String paymentStatus) {}
