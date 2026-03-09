/* (C) 2026 */
package aros.services.rms.infraestructure.area.api;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaType;
import aros.services.rms.core.area.port.input.AreaUseCase;
import aros.services.rms.infraestructure.area.api.dto.AreaRequest;
import aros.services.rms.infraestructure.area.api.dto.AreaResponse;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

/** REST controller for preparation area management. */
@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
@Tag(name = "Areas", description = "Preparation area management")
public class AreaController {

  private final AreaUseCase areaUseCase;

  @Operation(
      summary = "Create new area",
      description = "Creates a new preparation area with the given name and type.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Area created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Area name already exists")
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

  @Operation(
      summary = "Update area",
      description = "Updates an existing preparation area's name and type.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Area updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Area not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<AreaResponse> update(
      @PathVariable Long id, @Valid @RequestBody AreaRequest request) {
    Area area =
        Area.builder()
            .name(request.name())
            .type(AreaType.valueOf(request.type().toUpperCase()))
            .build();

    Area updated = areaUseCase.update(id, area);
    return ResponseEntity.ok(AreaResponse.fromDomain(updated));
  }

  @Operation(
      summary = "Get all areas",
      description = "Retrieves all preparation areas.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Areas retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<AreaResponse>> findAll() {
    List<AreaResponse> responses =
        areaUseCase.findAll().stream().map(AreaResponse::fromDomain).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @Operation(
      summary = "Get area by ID",
      description = "Retrieves a preparation area by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Area retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Area not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<AreaResponse> findById(@PathVariable Long id) {
    Area area = areaUseCase.findById(id);
    return ResponseEntity.ok(AreaResponse.fromDomain(area));
  }

  @Operation(
      summary = "Toggle area enabled status",
      description = "Toggles the enabled/disabled status of a preparation area.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Area status toggled successfully"),
        @ApiResponse(responseCode = "404", description = "Area not found")
      })
  @PutMapping("/{id}/toggle")
  public ResponseEntity<AreaResponse> toggleEnabled(@PathVariable Long id) {
    Area area = areaUseCase.toggleEnabled(id);
    return ResponseEntity.ok(AreaResponse.fromDomain(area));
  }
}
