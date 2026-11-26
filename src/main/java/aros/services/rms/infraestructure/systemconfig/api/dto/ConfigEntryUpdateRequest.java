/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** Request payload for a partial configuration entry update. */
@Schema(description = "Partial configuration entry update")
public record ConfigEntryUpdateRequest(
    @Schema(description = "New value", example = "USD")
        @Size(max = 500, message = "El valor no debe superar los 500 caracteres")
        String value,
    @Schema(description = "New description", example = "Moneda del sistema")
        @Size(max = 200, message = "La descripción no debe superar los 200 caracteres")
        String description,
    @Schema(description = "Target group id", example = "1") Long groupId,
    @Schema(description = "Display order within the group", example = "2") Integer sortOrder) {}
