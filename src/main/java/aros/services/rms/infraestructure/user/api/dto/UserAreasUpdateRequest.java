/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

/** Request payload to replace a user's assigned areas. */
@Schema(description = "Area reassignment")
public record UserAreasUpdateRequest(
    @Schema(description = "New area ids; an empty set removes every area", example = "[2, 3]")
        @NotNull(message = "Area ids are required")
        Set<Long> areaIds) {}
