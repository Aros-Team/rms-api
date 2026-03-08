/* (C) 2026 */
package aros.services.rms.infraestructure.table.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating or updating a table.
 *
 * @param tableNumber the table number
 * @param capacity the table capacity
 */
public record TableRequest(
    @NotNull(message = "Table number is required") @Positive(message = "Table number must be positive")
        Integer tableNumber,
    @NotNull(message = "Capacity is required") @Positive(message = "Capacity must be positive")
        Integer capacity) {}