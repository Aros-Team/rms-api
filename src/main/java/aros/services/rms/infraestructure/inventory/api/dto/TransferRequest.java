/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Request DTO for inventory transfer operation. */
public record TransferRequest(
    @NotEmpty(message = "Items list cannot be empty") @Valid List<TransferItemRequest> items) {}
