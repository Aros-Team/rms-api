/* (C) 2026 */

package aros.services.rms.infraestructure.area.api;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaType;
import aros.services.rms.core.area.port.input.AreaUseCase;
import aros.services.rms.infraestructure.area.api.dto.AreaRequest;
import aros.services.rms.infraestructure.area.api.dto.AreaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for preparation area management. */
@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
@Tag(
    name = "Areas",
    description = "Operations for managing restaurant preparation areas and their enabled status")
public class AreaController {

  private final AreaUseCase areaUseCase;

  /**
   * Creates a new area.
   *
   * @param request the area request
   * @return the created area
   */
  @Operation(
      tags = {"Areas"},
      summary = "Create new area",
      description = "Creates a new preparation area with the specified name and type.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Area created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Area name already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<AreaResponse> create(@Valid @RequestBody AreaRequest request) {
    Area area =
        Area.builder()
            .name(request.name())
            .type(AreaType.valueOf(request.type().toUpperCase()))
            .build();

    Area created = areaUseCase.create(area);
    return new ResponseEntity<>(AreaResponse.fromDomain(created), HttpStatus.CREATED);
  }

  /**
   * Updates an area.
   *
   * @param id the area ID
   * @param request the area request
   * @return the updated area
   */
  @Operation(
      tags = {"Areas"},
      summary = "Update area",
      description = "Updates the name and type of an existing preparation area.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Area updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Area not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<AreaResponse> update(
      @Parameter(description = "Area ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody AreaRequest request) {
    Area area =
        Area.builder()
            .name(request.name())
            .type(AreaType.valueOf(request.type().toUpperCase()))
            .build();

    Area updated = areaUseCase.update(id, area);
    return ResponseEntity.ok(AreaResponse.fromDomain(updated));
  }

  /**
   * Gets all areas, optionally filtered by name.
   *
   * @param search optional name filter (partial, case-insensitive)
   * @return the list of areas
   */
  @Operation(
      tags = {"Areas"},
      summary = "Get all areas",
      description =
          "Returns all preparation areas in the restaurant. "
              + "Optionally filters by name (partial, case-insensitive).",
      responses = {
        @ApiResponse(responseCode = "200", description = "Areas retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<AreaResponse>> findAll(
      @Parameter(
              description = "Optional name filter (partial, case-insensitive)",
              example = "kitchen")
          @RequestParam(required = false)
          String search) {
    List<Area> areas;
    if (search != null && !search.isBlank()) {
      areas = areaUseCase.findByNameContainingIgnoreCase(search);
    } else {
      areas = areaUseCase.findAll();
    }
    List<AreaResponse> responses =
        areas.stream().map(AreaResponse::fromDomain).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets an area by ID.
   *
   * @param id the area ID
   * @return the area
   */
  @Operation(
      tags = {"Areas"},
      summary = "Get area by ID",
      description = "Returns a specific preparation area by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Area retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Area not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<AreaResponse> findById(
      @Parameter(description = "Area ID", example = "1", required = true) @PathVariable Long id) {
    Area area = areaUseCase.findById(id);
    return ResponseEntity.ok(AreaResponse.fromDomain(area));
  }

  /**
   * Toggles area enabled status.
   *
   * @param id the area ID
   * @return the updated area
   */
  @Operation(
      tags = {"Areas"},
      summary = "Toggle area enabled status",
      description = "Toggles the enabled/disabled status of a preparation area.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Area status changed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Area not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/toggle")
  public ResponseEntity<AreaResponse> toggleEnabled(
      @Parameter(description = "Area ID", example = "1", required = true) @PathVariable Long id) {
    Area area = areaUseCase.toggleEnabled(id);
    return ResponseEntity.ok(AreaResponse.fromDomain(area));
  }
}
