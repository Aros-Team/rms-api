/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.api.dto;

import aros.services.rms.core.purchase.domain.Supplier;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for supplier data. */
@Schema(description = "Supplier data returned by the API")
public record SupplierResponse(
    @Schema(description = "Supplier ID", example = "1") Long id,
    @Schema(description = "Supplier name", example = "Distribuidora El Mayorista") String name,
    @Schema(description = "Contact phone or email", example = "3001234567") String contact,
    @Schema(description = "Whether the supplier is active", example = "true") boolean active) {

  /** Converts a Supplier domain object to a SupplierResponse DTO. */
  public static SupplierResponse fromDomain(Supplier supplier) {
    if (supplier == null) {
      return null;
    }
    return new SupplierResponse(
        supplier.getId(), supplier.getName(), supplier.getContact(), supplier.isActive());
  }
}
