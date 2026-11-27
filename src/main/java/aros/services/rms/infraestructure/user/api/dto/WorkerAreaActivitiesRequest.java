/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

/** Replaces the activities a worker performs in each assigned area. */
@Schema(description = "Worker activity assignments by area")
public record WorkerAreaActivitiesRequest(
    @Schema(description = "Area ID -> activities", example = "{\"3\":[\"PREPARATION\",\"SERVE\"]}")
        @NotNull(message = "Las actividades son obligatorias")
        Map<Long, Set<String>> areaActivities) {}
