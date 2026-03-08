/* (C) 2026 */
package aros.services.rms.infraestructure.table.api.dto;

import aros.services.rms.core.table.domain.Table;

/**
 * Response DTO for table data.
 *
 * @param id the table id
 * @param tableNumber the table number
 * @param capacity the table capacity
 * @param status the table status
 */
public record TableResponse(Long id, Integer tableNumber, Integer capacity, String status) {

  /** Converts a domain Table to a TableResponse DTO. */
  public static TableResponse fromDomain(Table table) {
    if (table == null) return null;
    return new TableResponse(
        table.getId(),
        table.getTableNumber(),
        table.getCapacity(),
        table.getStatus() != null ? table.getStatus().name() : null);
  }
}