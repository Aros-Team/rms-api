/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** Request payload to create or update a configuration group. */
@Schema(description = "Configuration group data")
public record ConfigGroupRequest(
    @Schema(description = "Group display name", example = "Notificaciones")
        @Size(max = 80, message = "Group name must not exceed 80 characters")
        String name,
    @Schema(description = "Group description", example = "Preferencias de alertas")
        @Size(max = 200, message = "Description must not exceed 200 characters")
        String description,
    @Schema(description = "Display order among groups", example = "4") Integer sortOrder) {}
