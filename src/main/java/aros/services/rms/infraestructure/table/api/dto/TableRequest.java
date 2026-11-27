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
        @NotNull(message = "El número de mesa es obligatorio")
        @Positive(message = "El número de mesa debe ser positivo")
        Integer tableNumber,
    @Schema(description = "Table capacity (number of seats)", example = "4")
        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser positiva")
        Integer capacity) {}
