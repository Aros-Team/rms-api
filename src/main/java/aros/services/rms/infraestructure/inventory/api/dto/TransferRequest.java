/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Request DTO for inventory transfer operation. */
@Schema(
    description =
        "Request payload to transfer one or more supply variants from Warehouse to Kitchen")
public record TransferRequest(
    @Schema(
            description = "List of transfer items (at least one)",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Items list cannot be empty")
        @Valid
        List<TransferItemRequest> items) {}
