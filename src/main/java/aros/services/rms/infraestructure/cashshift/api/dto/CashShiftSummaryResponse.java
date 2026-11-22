/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.api.dto;

import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Response DTO for the shift Z-summary (open or closed). */
@Schema(description = "Cash shift summary: expected vs counted")
public record CashShiftSummaryResponse(
    @Schema(description = "Shift ID") Long id,
    @Schema(description = "Status", example = "CLOSED") String status,
    @Schema(description = "Currency", example = "COP") String currency,
    @Schema(description = "User who opened the shift") Long openedBy,
    @Schema(description = "Open timestamp") LocalDateTime openedAt,
    @Schema(description = "User who closed the shift") Long closedBy,
    @Schema(description = "Close timestamp") LocalDateTime closedAt,
    @Schema(description = "Opening float") MoneyDto openingFloat,
    @Schema(description = "Completed payments") long paymentsCount,
    @Schema(description = "Voided payments") long voidedCount,
    @Schema(description = "Sum of subtotals") MoneyDto subtotal,
    @Schema(description = "Sum of discounts") MoneyDto discounts,
    @Schema(description = "Sum of tips") MoneyDto tips,
    @Schema(description = "Sum of totals charged") MoneyDto totalCharged,
    @Schema(description = "Cash tendered") MoneyDto cash,
    @Schema(description = "Card tendered") MoneyDto card,
    @Schema(description = "Transfer tendered") MoneyDto transfer,
    @Schema(description = "Change returned") MoneyDto changeGiven,
    @Schema(description = "Manual cash in") MoneyDto paidIn,
    @Schema(description = "Manual cash out") MoneyDto paidOut,
    @Schema(description = "Cash that should be in the drawer") MoneyDto expectedCash,
    @Schema(description = "Cash counted by the cashier") MoneyDto countedCash,
    @Schema(description = "counted - expected") MoneyDto difference,
    @Schema(description = "Close notes") String notes) {}
