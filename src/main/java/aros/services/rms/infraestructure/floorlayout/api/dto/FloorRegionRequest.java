/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api.dto;

import aros.services.rms.core.floorlayout.domain.FloorLayoutLimits;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** One submitted floor region. */
@Schema(description = "Independent region linked to an existing area")
public record FloorRegionRequest(
    @Schema(description = "Stable client-generated region key", example = "6f0c2b4e-...")
        @NotBlank(message = "Cada región debe tener un identificador")
        @Size(
            max = FloorLayoutLimits.MAX_REGION_KEY_LENGTH,
            message = "El identificador de la región es demasiado largo")
        String key,
    @Schema(description = "Referenced area id", example = "3")
        @NotNull(message = "Cada región debe estar vinculada a un área")
        Long areaId,
    @Schema(description = "Painted row runs")
        @NotEmpty(message = "Cada región debe tener al menos una celda")
        @Valid
        List<FloorSegmentDto> segments) {}
