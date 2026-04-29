/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api.dto;

import aros.services.rms.infraestructure.inventory.persistence.SupplyEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for a supply (insumo base). */
@Schema(description = "Supply (insumo) data")
public record SupplyResponse(
    @Schema(description = "Supply ID", example = "3") Long id,
    @Schema(description = "Supply name", example = "Carne de Res") String name,
    @Schema(description = "Category ID", example = "1") Long categoryId,
    @Schema(description = "Category name", example = "Proteínas") String categoryName) {

  /**
   * Creates a response from an entity.
   *
   * @param entity the entity
   * @return the response DTO
   */
  public static SupplyResponse fromEntity(SupplyEntity entity) {
    if (entity == null) return null;
    return new SupplyResponse(
        entity.getId(),
        entity.getName(),
        entity.getCategory() != null ? entity.getCategory().getId() : null,
        entity.getCategory() != null ? entity.getCategory().getName() : null);
  }
}
