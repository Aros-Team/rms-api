/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api.dto;

import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Compact payment row for the journal, shift view and audit screens. */
@Schema(description = "Payment summary")
public record PaymentSummaryResponse(
    @Schema(description = "Payment ID") Long id,
    @Schema(description = "Order ID") Long orderId,
    @Schema(description = "Payment status") String status,
    @Schema(description = "Receipt number") String receiptNumber,
    @Schema(description = "Currency") String currency,
    @Schema(description = "Server-computed subtotal") MoneyDto subtotal,
    @Schema(description = "Discount") MoneyDto discount,
    @Schema(description = "Tip") MoneyDto tip,
    @Schema(description = "Total charged") MoneyDto total,
    @Schema(description = "Cashier user ID") Long cashierId,
    @Schema(description = "Cash shift ID") Long shiftId,
    @Schema(description = "Completion timestamp") LocalDateTime completedAt,
    @Schema(description = "Void timestamp") LocalDateTime voidedAt,
    @Schema(description = "Void reason") String voidReason) {}
