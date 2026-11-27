/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api.dto;

import aros.services.rms.core.floorlayout.domain.FloorLayoutLimits;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Request body for replacing the complete floor layout. */
@Schema(description = "Complete floor layout replacement")
public record FloorLayoutRequest(
    @Schema(description = "Grid width in cells", example = "32")
        @Min(value = FloorLayoutLimits.MIN_COLUMNS, message = "El lienzo es demasiado estrecho")
        @Max(value = FloorLayoutLimits.MAX_COLUMNS, message = "El lienzo es demasiado ancho")
        int columns,
    @Schema(description = "Grid height in cells", example = "20")
        @Min(value = FloorLayoutLimits.MIN_ROWS, message = "El lienzo tiene pocas filas")
        @Max(value = FloorLayoutLimits.MAX_ROWS, message = "El lienzo tiene demasiadas filas")
        int rows,
    @Schema(description = "Regions painted on the grid")
        @NotNull(message = "La lista de regiones es obligatoria")
        @Valid
        List<FloorRegionRequest> regions) {}
