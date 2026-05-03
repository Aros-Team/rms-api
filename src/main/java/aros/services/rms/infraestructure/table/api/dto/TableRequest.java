/* (C) 2026 */

package aros.services.rms.infraestructure.table.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request DTO for creating or updating a table. */
@Schema(
    description = "Request DTO for creating or updating a table",
    example = "{\"tableNumber\": 5, \"capacity\": 4}")
public record TableRequest(
    @Schema(description = "Table number", example = "5")
        @NotNull(message = "Table number is required")
        @Positive(message = "Table number must be positive")
        Integer tableNumber,
    @Schema(description = "Table capacity (number of seats)", example = "4")
        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be positive")
        Integer capacity) {}
