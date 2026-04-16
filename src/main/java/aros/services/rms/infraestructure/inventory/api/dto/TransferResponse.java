/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api.dto;

import aros.services.rms.core.inventory.domain.InventoryMovement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Response DTO for a registered inventory transfer movement. */
public record TransferResponse(
    Long id,
    Long supplyVariantId,
    Long fromStorageLocationId,
    Long toStorageLocationId,
    BigDecimal quantity,
    String movementType,
    LocalDateTime createdAt) {

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
