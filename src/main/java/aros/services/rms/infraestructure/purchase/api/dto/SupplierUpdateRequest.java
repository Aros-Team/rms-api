/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing supplier. Includes the active flag so the client can
 * activate or deactivate a supplier in the same operation.
 */
@Schema(
    description = "Request payload for updating a supplier",
    example =
        """
        {
          "name": "Distribuidora El Mayorista",
          "contact": "3001234567",
          "active": true
        }
        """)
public record SupplierUpdateRequest(
    @Schema(description = "Supplier name", example = "Distribuidora El Mayorista")
        @NotBlank(message = "Supplier name is required") @Size(max = 255, message = "Name must not exceed 255 characters") String name,
    @Schema(description = "Contact phone or email", example = "3001234567")
        @Size(max = 255, message = "Contact must not exceed 255 characters") String contact,
    @Schema(description = "Whether the supplier is active", example = "true")
        @NotNull(message = "Active status is required") Boolean active) {}
