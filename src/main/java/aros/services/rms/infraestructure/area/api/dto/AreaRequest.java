/* (C) 2026 */

package aros.services.rms.infraestructure.area.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/** Request DTO for creating or updating a preparation area. */
@Schema(
    description = "Request DTO for creating or updating a preparation area",
    example =
        "{\"name\": \"Cocina Principal\", \"activities\": [\"PREPARATION\"], \"exclusive\": true}")
public record AreaRequest(
    @Schema(description = "Area name", example = "Cocina Principal")
        @NotBlank(message = "El nombre del área es obligatorio")
        String name,
    @Schema(description = "Activities allowed in the area", example = "[\"PREPARATION\"]")
        @NotEmpty(message = "Selecciona al menos una actividad")
        Set<String> activities,
    @Schema(description = "An exclusive area cannot be combined with other areas", example = "true")
        boolean exclusive) {}
