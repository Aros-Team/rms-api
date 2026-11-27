/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorRegion;
import aros.services.rms.core.floorlayout.domain.FloorSegment;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import aros.services.rms.core.floorlayout.port.input.FloorLayoutUseCase;
import aros.services.rms.infraestructure.floorlayout.api.dto.FloorLayoutRequest;
import aros.services.rms.infraestructure.floorlayout.api.dto.FloorLayoutResponse;
import aros.services.rms.infraestructure.floorlayout.api.dto.FloorRegionRequest;
import aros.services.rms.infraestructure.floorlayout.api.dto.FloorSegmentDto;
import aros.services.rms.infraestructure.floorlayout.api.dto.FloorTablePlacementDto;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the restaurant floor layout. */
@RestController
@RequestMapping("/api/v1/floor-layout")
@RequiredArgsConstructor
@Tag(
    name = "Floor Layout",
    description = "Schematic distribution of the restaurant areas on a grid")
@JustAdminUser
public class FloorLayoutController {

  private final FloorLayoutUseCase floorLayoutUseCase;

  /**
   * Returns the current layout.
   *
   * @return stored layout or documented defaults
   */
  @Operation(
      tags = {"Floor Layout"},
      summary = "Get floor layout",
      description =
          "Returns the restaurant grid dimensions and every region linked to an existing area.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Layout retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @GetMapping
  public ResponseEntity<FloorLayoutResponse> get() {
    return ResponseEntity.ok(FloorLayoutResponse.fromDomain(floorLayoutUseCase.get()));
  }

  /**
   * Validates and replaces the complete layout.
   *
   * @param request complete layout
   * @return canonical persisted layout
   */
  @Operation(
      tags = {"Floor Layout"},
      summary = "Replace floor layout",
      description =
          "Validates the complete document and atomically replaces the stored layout and regions. "
              + "Regions cannot overlap and every region must reference an existing area.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Layout saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid layout or unknown area"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping
  public ResponseEntity<FloorLayoutResponse> save(@Valid @RequestBody FloorLayoutRequest request) {
    return ResponseEntity.ok(
        FloorLayoutResponse.fromDomain(floorLayoutUseCase.save(toDomain(request))));
  }

  private FloorLayout toDomain(FloorLayoutRequest request) {
    List<FloorRegion> regions = request.regions().stream().map(this::toDomain).toList();
    return FloorLayout.builder()
        .columns(request.columns())
        .rows(request.rows())
        .regions(regions)
        .build();
  }

  private FloorRegion toDomain(FloorRegionRequest request) {
    List<FloorSegment> segments =
        request.segments().stream().map(FloorLayoutController::toDomain).toList();
    return FloorRegion.builder()
        .key(request.key())
        .areaId(request.areaId())
        .segments(segments)
        .build();
  }

  private static FloorSegment toDomain(FloorSegmentDto segment) {
    return new FloorSegment(segment.row(), segment.from(), segment.to());
  }

  /**
   * Returns table placements.
   *
   * @return placements
   */
  @Operation(
      tags = {"Floor Layout"},
      summary = "Get table placements",
      description = "Returns every table placed on the floor grid.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Placements retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
      })
  @GetMapping("/tables")
  public ResponseEntity<List<FloorTablePlacementDto>> getTablePlacements() {
    return ResponseEntity.ok(
        floorLayoutUseCase.getTablePlacements().stream()
            .map(FloorTablePlacementDto::fromDomain)
            .toList());
  }

  /**
   * Validates and replaces every table placement.
   *
   * @param placements complete placement list
   * @return saved placements
   */
  @Operation(
      tags = {"Floor Layout"},
      summary = "Replace table placements",
      description =
          "Validates that each table exists, uses one unique cell and lands inside an enabled "
              + "area with the SERVE activity, then replaces all placements atomically.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Placements saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid table placement"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/tables")
  public ResponseEntity<List<FloorTablePlacementDto>> saveTablePlacements(
      @Valid @RequestBody List<@Valid FloorTablePlacementDto> placements) {
    List<FloorTablePlacement> domain =
        placements.stream().map(FloorTablePlacementDto::toDomain).toList();
    return ResponseEntity.ok(
        floorLayoutUseCase.saveTablePlacements(domain).stream()
            .map(FloorTablePlacementDto::fromDomain)
            .toList());
  }
}
