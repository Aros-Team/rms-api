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
        @NotNull(message = "supplierId is required")
        Long supplierId,
    @Schema(description = "ID of the user registering the purchase", example = "2")
        @NotNull(message = "registeredById is required")
        Long registeredById,
    @Schema(description = "Date and time the purchase actually occurred", example = "2026-04-02T10:30:00")
        @NotNull(message = "purchasedAt is required")
        LocalDateTime purchasedAt,
    @Schema(description = "Total amount paid to the supplier", example = "120000.00")
        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "totalAmount must be >= 0")
        BigDecimal totalAmount,
    @Schema(description = "Optional notes about the purchase", example = "2 kg de carne llegaron en mal estado")
        String notes,
    @Schema(description = "List of purchased items (at least one required)")
        @NotEmpty(message = "At least one item is required")
        @Valid
        List<PurchaseOrderItemRequest> items) {}
