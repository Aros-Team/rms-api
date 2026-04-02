/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.api.dto;

import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Response DTO for a single purchase order line item. */
@Schema(description = "A single item in a purchase order response")
public record PurchaseOrderItemResponse(
    @Schema(description = "Item ID", example = "10") Long id,
    @Schema(description = "Supply variant ID", example = "3") Long supplyVariantId,
    @Schema(description = "Quantity ordered / invoiced", example = "10.000") BigDecimal quantityOrdered,
    @Schema(description = "Quantity received in good condition", example = "8.000") BigDecimal quantityReceived,
    @Schema(description = "Unit price paid", example = "15000.00") BigDecimal unitPrice) {

  /** Converts a PurchaseOrderItem domain object to a response DTO. */
  public static PurchaseOrderItemResponse fromDomain(PurchaseOrderItem item) {
    if (item == null) return null;
    return new PurchaseOrderItemResponse(
        item.getId(),
        item.getSupplyVariantId(),
        item.getQuantityOrdered(),
        item.getQuantityReceived(),
        item.getUnitPrice());
  }
}
