/* (C) 2026 */
package aros.services.rms.infraestructure.table.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for changing a table's status.
 *
 * @param status the new status (AVAILABLE, OCCUPIED, RESERVED)
 */
public record ChangeStatusRequest(
    @NotBlank(message = "Status is required") String status) {}