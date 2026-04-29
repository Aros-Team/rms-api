/* (C) 2026 */
package aros.services.rms.infraestructure.table.api.dto;

import aros.services.rms.core.table.domain.Table;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for table data. */
@Schema(description = "Response DTO for table data")
public record TableResponse(
    @Schema(description = "Table ID", example = "1") Long id,
    @Schema(description = "Table number", example = "5") Integer tableNumber,
    @Schema(description = "Table capacity", example = "4") Integer capacity,
    @Schema(description = "Table status: AVAILABLE, OCCUPIED, RESERVED", example = "AVAILABLE")
        String status) {

  /** Converts a Table domain object to a TableResponse DTO. */
  public static TableResponse fromDomain(Table table) {
    if (table == null) return null;
    return new TableResponse(
        table.getId(),
        table.getTableNumber(),
        table.getCapacity(),
        table.getStatus() != null ? table.getStatus().name() : null);
  }
}
