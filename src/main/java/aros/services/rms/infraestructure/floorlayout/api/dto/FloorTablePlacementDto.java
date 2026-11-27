/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api.dto;

import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request/response for one table placement. */
@Schema(description = "Table placed on one floor-grid cell")
public record FloorTablePlacementDto(
    @Schema(description = "Table id", example = "1") @NotNull Long tableId,
    @Schema(description = "Zero-based row", example = "4") @Min(0) int row,
    @Schema(description = "Zero-based column", example = "8") @Min(0) int column) {

  /**
   * Creates a DTO from the domain.
   *
   * @param placement domain placement
   * @return API DTO
   */
  public static FloorTablePlacementDto fromDomain(FloorTablePlacement placement) {
    return new FloorTablePlacementDto(
        placement.getTableId(), placement.getRow(), placement.getColumn());
  }

  /**
   * Converts this DTO to the domain.
   *
   * @return domain placement
   */
  public FloorTablePlacement toDomain() {
    return FloorTablePlacement.builder().tableId(tableId).row(row).column(column).build();
  }
}
