/* (C) 2026 */

package aros.services.rms.infraestructure.area.api.dto;

import aros.services.rms.core.area.domain.Area;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO for area data. */
@Schema(description = "Response DTO for area data")
public record AreaResponse(
    @Schema(description = "Area ID", example = "1") Long id,
    @Schema(description = "Area name", example = "Cocina Principal") String name,
    @Schema(description = "Activities allowed in the area", example = "[\"PREPARATION\"]")
        List<String> activities,
    @Schema(description = "Whether area is enabled", example = "true") boolean enabled,
    @Schema(description = "Whether the area cannot be combined with other areas", example = "true")
        boolean exclusive) {

  /**
   * Creates a response from a domain object.
   *
   * @param area the area
   * @return the response DTO
   */
  public static AreaResponse fromDomain(Area area) {
    if (area == null) {
      return null;
    }
    return new AreaResponse(
        area.getId(),
        area.getName(),
        area.getActivities() == null
            ? List.of()
            : area.getActivities().stream().map(Enum::name).sorted().toList(),
        area.isEnabled(),
        area.isExclusive());
  }
}
