/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.api.dto;

import aros.services.rms.infraestructure.inventory.persistence.UnitOfMeasureEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for a unit of measure. */
@Schema(description = "Unit of measure data")
public record UnitOfMeasureResponse(
    @Schema(description = "Unit ID", example = "2") Long id,
    @Schema(description = "Full name", example = "Kilogramos") String name,
    @Schema(description = "Abbreviation", example = "kg") String abbreviation) {

  /**
   * Creates a response from an entity.
   *
   * @param entity the entity
   * @return the response DTO
   */
  public static UnitOfMeasureResponse fromEntity(UnitOfMeasureEntity entity) {
    if (entity == null) {
      return null;
    }
    return new UnitOfMeasureResponse(entity.getId(), entity.getName(), entity.getAbbreviation());
  }
}
