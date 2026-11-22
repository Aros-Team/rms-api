/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api.dto;

import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/** Response DTO for a payment and its receipt. */
@Schema(description = "Payment detail and receipt")
public record PaymentResponse(
    @Schema(description = "Payment ID", example = "9001") Long id,
    @Schema(description = "Order ID", example = "201767") Long orderId,
    @Schema(description = "Payment status", example = "COMPLETED") String status,
    @Schema(description = "Internal receipt number", example = "R-20261121-009001")
        String receiptNumber,
    @Schema(description = "Currency", example = "COP") String currency,
    @Schema(description = "Server-computed order subtotal") MoneyDto subtotal,
    @Schema(description = "Applied discount") MoneyDto discount,
    @Schema(description = "Tip") MoneyDto tip,
    @Schema(description = "Total charged") MoneyDto total,
    @Schema(description = "Sum of tenders") MoneyDto tendered,
    @Schema(description = "Change returned") MoneyDto change,
    @Schema(description = "Tenders") List<TenderResponse> tenders,
    @Schema(description = "Cashier user ID") Long cashierId,
    @Schema(description = "Cash shift ID") Long shiftId,
    @Schema(description = "Creation timestamp") LocalDateTime createdAt,
    @Schema(description = "Completion timestamp") LocalDateTime completedAt,
    @Schema(description = "Void timestamp") LocalDateTime voidedAt,
    @Schema(description = "User who voided the payment") Long voidedBy,
    @Schema(description = "Void reason") String voidReason) {

  /**
   * One tender of the payment.
   *
   * @param method the tender method
   * @param amount the tendered amount
   * @param reference optional reference
   */
  @Schema(description = "Payment tender")
  public record TenderResponse(String method, MoneyDto amount, String reference) {}
}
