/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api;

import aros.services.rms.core.floorlayout.port.input.FloorLayoutUseCase;
import aros.services.rms.infraestructure.floorlayout.api.dto.FloorLayoutMapResponse;
import aros.services.rms.infraestructure.share.security.JustWorkerOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only floor distribution for waiters and other operational staff. */
@RestController
@RequestMapping("/api/v1/floor-layout")
@RequiredArgsConstructor
@Tag(
    name = "Floor Layout",
    description = "Schematic distribution of the restaurant areas on a grid")
@JustWorkerOrAdmin
public class FloorLayoutMapController {

  private final FloorLayoutUseCase floorLayoutUseCase;

  /**
   * Returns the full distribution with the tables placed on it.
   *
   * @return layout and table placements
   */
  @Operation(
      tags = {"Floor Layout"},
      summary = "Get floor distribution map",
      description =
          "Read-only view for operational staff: painted areas, grid dimensions and every table "
              + "placed on the map.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Map retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Worker or admin access required")
      })
  @GetMapping("/map")
  public ResponseEntity<FloorLayoutMapResponse> getMap() {
    return ResponseEntity.ok(
        FloorLayoutMapResponse.fromDomain(
            floorLayoutUseCase.get(), floorLayoutUseCase.getTablePlacements()));
  }
}
