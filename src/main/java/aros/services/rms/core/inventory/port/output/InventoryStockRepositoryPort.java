/* (C) 2026 */

package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.InventoryStock;
import java.util.Optional;

/** Output port for inventory stock persistence operations. */
public interface InventoryStockRepositoryPort {

  /**
   * Finds inventory stock by variant and location with lock.
   *
   * @param variantId the supply variant identifier
   * @param locationId the storage location identifier
   * @return Optional containing the inventory stock if found
   */
  Optional<InventoryStock> findByVariantAndLocationWithLock(Long variantId, Long locationId);

  /**
   * Saves an inventory stock to the repository.
   *
   * @param inventoryStock the inventory stock to save
   * @return the saved inventory stock
   */
  InventoryStock save(InventoryStock inventoryStock);
}
