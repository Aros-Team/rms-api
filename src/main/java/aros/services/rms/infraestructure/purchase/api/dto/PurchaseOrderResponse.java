/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.api.dto;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Response DTO for a purchase order. */
@Schema(description = "Purchase order data returned by the API")
public record PurchaseOrderResponse(
    @Schema(description = "Purchase order ID", example = "5") Long id,
    @Schema(description = "Supplier ID", example = "1") Long supplierId,
    @Schema(description = "ID of the user who registered the purchase", example = "2") Long registeredById,
    @Schema(description = "Date and time the purchase occurred") LocalDateTime purchasedAt,
    @Schema(description = "Total amount paid to the supplier", example = "120000.00") BigDecimal totalAmount,
    @Schema(description = "Optional notes") String notes,
    @Schema(description = "Timestamp when the record was created") LocalDateTime createdAt,
    @Schema(description = "Line items of the purchase order") List<PurchaseOrderItemResponse> items) {

  /** Converts a PurchaseOrder domain object to a response DTO. */
  public static PurchaseOrderResponse fromDomain(PurchaseOrder order) {
    if (order == null) return null;
    var items =
        order.getItems() != null
            ? order.getItems().stream()
                .map(PurchaseOrderItemResponse::fromDomain)
                .collect(Collectors.toList())
            : Collections.<PurchaseOrderItemResponse>emptyList();
    return new PurchaseOrderResponse(
        order.getId(),
        order.getSupplierId(),
        order.getRegisteredById(),
        order.getPurchasedAt(),
        order.getTotalAmount(),
        order.getNotes(),
        order.getCreatedAt(),
        items);
  }
}
