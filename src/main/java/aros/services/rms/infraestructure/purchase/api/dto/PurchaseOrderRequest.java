/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Request DTO for registering a new purchase order. */
@Schema(description = "Request payload for registering a purchase order")
public record PurchaseOrderRequest(
    @Schema(description = "Supplier ID", example = "1")
        @NotNull(message = "supplierId es obligatorio")
        Long supplierId,
    @Schema(description = "ID of the user registering the purchase", example = "2")
        @NotNull(message = "registeredById es obligatorio")
        Long registeredById,
    @Schema(
            description = "Date and time the purchase actually occurred",
            example = "2026-04-02T10:30:00")
        @NotNull(message = "purchasedAt es obligatorio")
        LocalDateTime purchasedAt,
    @Schema(
            description =
                "Total amount paid to the supplier. Kept for compatibility; the server "
                    + "recomputes the authoritative total from the line items.",
            example = "120000.00")
        @NotNull(message = "totalAmount es obligatorio")
        @DecimalMin(value = "0.00", inclusive = true, message = "totalAmount debe ser >= 0")
        BigDecimal totalAmount,
    @Schema(
            description = "Optional notes about the purchase",
            example = "2 kg de carne llegaron en mal estado")
        String notes,
    @Schema(description = "List of purchased items (at least one required)")
        @NotEmpty(message = "Se requiere al menos un ítem")
        @Valid
        List<PurchaseOrderItemRequest> items) {}
