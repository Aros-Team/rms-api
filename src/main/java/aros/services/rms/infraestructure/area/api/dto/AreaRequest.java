/* (C) 2026 */

package aros.services.rms.infraestructure.area.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request DTO for creating or updating a preparation area. */
@Schema(
    description = "Request DTO for creating or updating a preparation area",
    example = "{\"name\": \"Cocina Principal\", \"type\": \"KITCHEN\"}")
public record AreaRequest(
    @Schema(description = "Area name", example = "Cocina Principal")
        @NotBlank(message = "Area name is required")
        String name,
    @Schema(description = "Area type: KITCHEN or BARTENDER", example = "KITCHEN")
        @NotNull(message = "Area type is required")
        String type) {}
