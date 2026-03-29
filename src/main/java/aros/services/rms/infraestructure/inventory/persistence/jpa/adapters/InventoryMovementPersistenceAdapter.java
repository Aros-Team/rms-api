/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.infraestructure.inventory.persistence.jpa.InventoryMovementMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter that connects InventoryMovementRepositoryPort with JPA repository. */
@Component
@RequiredArgsConstructor
public class InventoryMovementPersistenceAdapter implements InventoryMovementRepositoryPort {

  private final InventoryMovementRepository inventoryMovementRepository;
  private final InventoryMovementMapper inventoryMovementMapper;

  @Override
  @Transactional
  public InventoryMovement save(InventoryMovement movement) {
    return inventoryMovementMapper.toDomain(
        inventoryMovementRepository.save(inventoryMovementMapper.toEntity(movement)));
  }
}
