/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api.dto;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Response body for the floor layout. */
@Schema(description = "Current floor layout")
public record FloorLayoutResponse(
    @Schema(description = "Grid width in cells", example = "32") int columns,
    @Schema(description = "Grid height in cells", example = "20") int rows,
    @Schema(description = "Painted regions") List<FloorRegionResponse> regions,
    @Schema(description = "Last successful save", example = "2026-11-26T15:30:00Z")
        Instant updatedAt) {

  /**
   * Creates a response from the domain.
   *
   * @param layout domain layout
   * @return response DTO
   */
  public static FloorLayoutResponse fromDomain(FloorLayout layout) {
    if (layout == null) {
      return null;
    }
    List<FloorRegionResponse> regions =
        layout.getRegions() == null
            ? List.of()
            : layout.getRegions().stream().map(FloorRegionResponse::fromDomain).toList();
    return new FloorLayoutResponse(
        layout.getColumns(), layout.getRows(), regions, layout.getUpdatedAt());
  }
}
