/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** Request payload to create or update a configuration group. */
@Schema(description = "Configuration group data")
public record ConfigGroupRequest(
    @Schema(description = "Group display name", example = "Notificaciones")
        @Size(max = 80, message = "El nombre del grupo no debe superar los 80 caracteres")
        String name,
    @Schema(description = "Group description", example = "Preferencias de alertas")
        @Size(max = 200, message = "La descripción no debe superar los 200 caracteres")
        String description,
    @Schema(description = "Display order among groups", example = "4") Integer sortOrder) {}
