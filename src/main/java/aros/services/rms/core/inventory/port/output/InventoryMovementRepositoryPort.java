/* (C) 2026 */

package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.InventoryMovement;

/** Output port for inventory movement persistence operations. */
public interface InventoryMovementRepositoryPort {

  /**
   * Saves an inventory movement to the repository.
   *
   * @param movement the inventory movement to save
   * @return the saved inventory movement
   */
  InventoryMovement save(InventoryMovement movement);
}
