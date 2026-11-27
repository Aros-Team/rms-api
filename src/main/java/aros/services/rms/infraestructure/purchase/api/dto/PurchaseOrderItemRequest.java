/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request DTO for a single line item within a purchase order. */
@Schema(description = "A single item in a purchase order")
public record PurchaseOrderItemRequest(
    @Schema(description = "Supply variant ID", example = "3")
        @NotNull(message = "supplyVariantId es obligatorio")
        Long supplyVariantId,
    @Schema(description = "Quantity ordered / invoiced by the supplier", example = "10.000")
        @NotNull(message = "quantityOrdered es obligatorio")
        @DecimalMin(value = "0.001", message = "quantityOrdered debe ser mayor que 0")
        BigDecimal quantityOrdered,
    @Schema(description = "Quantity received in good condition (enters stock)", example = "8.000")
        @NotNull(message = "quantityReceived es obligatorio")
        @DecimalMin(value = "0.000", inclusive = true, message = "quantityReceived debe ser >= 0")
        BigDecimal quantityReceived,
    @Schema(description = "Unit price paid to the supplier", example = "15000.00")
        @NotNull(message = "unitPrice es obligatorio")
        @DecimalMin(value = "0.00", inclusive = true, message = "unitPrice debe ser >= 0")
        BigDecimal unitPrice) {}
