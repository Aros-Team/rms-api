/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api.dto;

import aros.services.rms.infraestructure.inventory.persistence.SupplyCategoryEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for a supply category. */
@Schema(description = "Supply category data")
public record SupplyCategoryResponse(
    @Schema(description = "Category ID", example = "1") Long id,
    @Schema(description = "Category name", example = "Proteínas") String name) {

  public static SupplyCategoryResponse fromEntity(SupplyCategoryEntity entity) {
    if (entity == null) return null;
    return new SupplyCategoryResponse(entity.getId(), entity.getName());
  }
}
