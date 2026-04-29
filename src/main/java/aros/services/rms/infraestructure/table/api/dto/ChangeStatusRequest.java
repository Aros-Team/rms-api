/* (C) 2026 */
package aros.services.rms.infraestructure.table.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for changing a table's status. */
@Schema(
    description = "Request DTO for changing a table's status",
    example = "{\"status\": \"OCCUPIED\"}")
public record ChangeStatusRequest(
    @Schema(description = "New status: AVAILABLE, OCCUPIED, or RESERVED", example = "OCCUPIED")
        @NotBlank(message = "Status is required") String status) {}
