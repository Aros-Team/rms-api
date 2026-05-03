/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Request DTO for updating the day menu. */
@Schema(description = "Request to update the active day menu")
public record UpdateDayMenuRequest(
    @Schema(description = "ID of the product to set as the day menu", example = "5")
        @NotNull(message = "productId es requerido")
        Long productId) {}
