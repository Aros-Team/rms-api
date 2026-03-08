/* (C) 2026 */
package aros.services.rms.infraestructure.area.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating or updating a preparation area.
 *
 * @param name the area name
 * @param type the area type (KITCHEN, BARTENDER)
 */
public record AreaRequest(
    @NotBlank(message = "Area name is required") String name,
    @NotNull(message = "Area type is required") String type
) {}

