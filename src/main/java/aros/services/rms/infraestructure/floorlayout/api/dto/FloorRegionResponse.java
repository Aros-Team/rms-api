/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api.dto;

import aros.services.rms.core.floorlayout.domain.FloorRegion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response body for one floor region. */
@Schema(description = "Independent region linked to an existing area")
public record FloorRegionResponse(
    @Schema(description = "Stable region key") String key,
    @Schema(description = "Referenced area id") Long areaId,
    @Schema(description = "Painted row runs") List<FloorSegmentDto> segments) {

  /**
   * Creates a response from the domain.
   *
   * @param region domain region
   * @return response DTO
   */
  public static FloorRegionResponse fromDomain(FloorRegion region) {
    if (region == null) {
      return null;
    }
    return new FloorRegionResponse(
        region.getKey(),
        region.getAreaId(),
        region.getSegments() == null
            ? List.of()
            : region.getSegments().stream()
                .map(segment -> new FloorSegmentDto(segment.row(), segment.from(), segment.to()))
                .toList());
  }
}
