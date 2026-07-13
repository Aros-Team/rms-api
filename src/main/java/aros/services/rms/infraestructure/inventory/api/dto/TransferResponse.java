/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.api.dto;

import aros.services.rms.core.inventory.domain.InventoryMovement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Response DTO for a registered inventory transfer movement. */
@Schema(description = "Response DTO for an inventory transfer movement")
public record TransferResponse(
    @Schema(description = "Movement ID", example = "1") Long id,
    @Schema(description = "Transferred supply variant ID", example = "3") Long supplyVariantId,
    @Schema(description = "Source storage location ID (Warehouse)", example = "1")
        Long fromStorageLocationId,
    @Schema(description = "Destination storage location ID (Kitchen)", example = "2")
        Long toStorageLocationId,
    @Schema(description = "Quantity transferred", example = "5.000") BigDecimal quantity,
    @Schema(description = "Movement type", example = "TRANSFER") String movementType,
    @Schema(description = "Movement timestamp", example = "2026-07-13T10:30:00")
        LocalDateTime createdAt) {

  /**
   * Creates a response from a domain object.
   *
   * @param movement the movement domain
   * @return the response DTO
   */
  public static TransferResponse fromDomain(InventoryMovement movement) {
    return new TransferResponse(
        movement.getId(),
        movement.getSupplyVariantId(),
        movement.getFromStorageLocationId(),
        movement.getToStorageLocationId(),
        movement.getQuantity(),
        movement.getMovementType().name(),
        movement.getCreatedAt());
  }
}
