/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO for creating or updating a supplier. */
@Schema(
    description = "Request payload for creating or updating a supplier",
    example =
        """
        {
          "name": "Distribuidora El Mayorista",
          "contact": "3001234567"
        }
        """)
public record SupplierRequest(
    @Schema(description = "Supplier name", example = "Distribuidora El Mayorista")
        @NotBlank(message = "Supplier name is required") @Size(max = 255, message = "Name must not exceed 255 characters") String name,
    @Schema(description = "Contact phone or email", example = "3001234567")
        @Size(max = 255, message = "Contact must not exceed 255 characters") String contact) {}
