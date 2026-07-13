/* (C) 2026 */

package aros.services.rms.infraestructure.table.api;

import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.input.TableUseCase;
import aros.services.rms.infraestructure.table.api.dto.ChangeStatusRequest;
import aros.services.rms.infraestructure.table.api.dto.TableRequest;
import aros.services.rms.infraestructure.table.api.dto.TableResponse;
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
import org.springframework.web.bind.annotation.RestController;

/** REST controller for table management. */
@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@Tag(
    name = "Tables",
    description = "Operations for managing restaurant tables and their lifecycle status")
public class TableController {

  private final TableUseCase tableUseCase;
  private final TableNotificationService tableNotificationService;

  /** Creates a new table. */
  @Operation(
      tags = {"Tables"},
      summary = "Create new table",
      description = "Creates a new table with the specified number and capacity.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Table created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Table number already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
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
      tags = {"Tables"},
      summary = "Update table",
      description = "Updates the number and capacity of an existing table.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Table updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<TableResponse> update(
      @Parameter(description = "Table ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody TableRequest request) {
    Table table =
        Table.builder().tableNumber(request.tableNumber()).capacity(request.capacity()).build();

    Table updated = tableUseCase.update(id, table);
    return ResponseEntity.ok(TableResponse.fromDomain(updated));
  }

  /** Retrieves all tables. */
  @Operation(
      tags = {"Tables"},
      summary = "Get all tables",
      description = "Returns all tables in the restaurant.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Tables retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<TableResponse>> findAll() {
    List<TableResponse> responses =
        tableUseCase.findAll().stream().map(TableResponse::fromDomain).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /** Retrieves a table by ID. */
  @Operation(
      tags = {"Tables"},
      summary = "Get table by ID",
      description = "Returns a specific table by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Table retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<TableResponse> findById(
      @Parameter(description = "Table ID", example = "1", required = true) @PathVariable Long id) {
    Table table = tableUseCase.findById(id);
    return ResponseEntity.ok(TableResponse.fromDomain(table));
  }

  /** Changes the table status. */
  @Operation(
      tags = {"Tables"},
      summary = "Change table status",
      description =
          "Changes the table status among AVAILABLE, OCCUPIED, and RESERVED. "
              + "Triggers a real-time notification to subscribed clients via WebSocket.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Table status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/status")
  public ResponseEntity<TableResponse> changeStatus(
      @Parameter(description = "Table ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody ChangeStatusRequest request) {
    TableStatus status = TableStatus.valueOf(request.status().toUpperCase());
    Table table = tableUseCase.changeStatus(id, status);
    TableResponse response = TableResponse.fromDomain(table);
    tableNotificationService.notifyTableStatusChanged(response);
    return ResponseEntity.ok(response);
  }
}
