/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api.dto;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Read-only view of the floor distribution for operational staff. */
@Schema(description = "Restaurant floor distribution with the tables placed on it")
public record FloorLayoutMapResponse(
    @Schema(description = "Painted areas and grid dimensions") FloorLayoutResponse layout,
    @Schema(description = "Tables placed on the grid") List<FloorTablePlacementDto> tables) {

  /**
   * Creates a map response from the domain.
   *
   * @param layout current layout
   * @param placements table placements
   * @return response DTO
   */
  public static FloorLayoutMapResponse fromDomain(
      FloorLayout layout, List<FloorTablePlacement> placements) {
    return new FloorLayoutMapResponse(
        FloorLayoutResponse.fromDomain(layout),
        placements == null
            ? List.of()
            : placements.stream().map(FloorTablePlacementDto::fromDomain).toList());
  }
}
