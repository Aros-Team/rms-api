/* (C) 2026 */
package aros.services.rms.infraestructure.table.api;

import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.input.TableUseCase;
import aros.services.rms.infraestructure.table.api.dto.ChangeStatusRequest;
import aros.services.rms.infraestructure.table.api.dto.TableRequest;
import aros.services.rms.infraestructure.table.api.dto.TableResponse;
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

/** REST controller for table management. */
@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@Tag(name = "Tables", description = "Table management and status lifecycle")
public class TableController {

  private final TableUseCase tableUseCase;

  /** Creates a new table. */
  @Operation(
      summary = "Create new table",
      description = "Creates a new table with the given number and capacity.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Table created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  @PostMapping
  public ResponseEntity<TableResponse> create(@Valid @RequestBody TableRequest request) {
    Table table =
        Table.builder().tableNumber(request.tableNumber()).capacity(request.capacity()).build();

    Table created = tableUseCase.create(table);
    return new ResponseEntity<>(TableResponse.fromDomain(created), HttpStatus.CREATED);
  }

  /** Updates an existing table. */
  @Operation(
      summary = "Update table",
      description = "Updates an existing table's number and capacity.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Table updated successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<TableResponse> update(
      @PathVariable Long id, @Valid @RequestBody TableRequest request) {
    Table table =
        Table.builder().tableNumber(request.tableNumber()).capacity(request.capacity()).build();

    Table updated = tableUseCase.update(id, table);
    return ResponseEntity.ok(TableResponse.fromDomain(updated));
  }

  /** Retrieves all tables. */
  @Operation(
      summary = "Get all tables",
      description = "Retrieves all tables.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Tables retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<TableResponse>> findAll() {
    List<TableResponse> responses =
        tableUseCase.findAll().stream().map(TableResponse::fromDomain).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /** Retrieves a table by ID. */
  @Operation(
      summary = "Get table by ID",
      description = "Retrieves a table by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Table retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<TableResponse> findById(@PathVariable Long id) {
    Table table = tableUseCase.findById(id);
    return ResponseEntity.ok(TableResponse.fromDomain(table));
  }

  /** Changes the table status. */
  @Operation(
      summary = "Change table status",
      description = "Changes the table status between AVAILABLE, OCCUPIED, and RESERVED.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Table status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition")
      })
  @PutMapping("/{id}/status")
  public ResponseEntity<TableResponse> changeStatus(
      @PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
    TableStatus status = TableStatus.valueOf(request.status().toUpperCase());
    Table table = tableUseCase.changeStatus(id, status);
    return ResponseEntity.ok(TableResponse.fromDomain(table));
  }
}
