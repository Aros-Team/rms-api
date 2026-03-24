/* (C) 2026 */
package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.InventoryMovement;

/** Output port for inventory movement persistence operations. */
public interface InventoryMovementRepositoryPort {

  InventoryMovement save(InventoryMovement movement);
}
