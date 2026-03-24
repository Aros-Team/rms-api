/* (C) 2026 */
package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.InventoryStock;
import java.util.Optional;

/** Output port for inventory stock persistence operations. */
public interface InventoryStockRepositoryPort {

  Optional<InventoryStock> findByVariantAndLocationWithLock(Long variantId, Long locationId);

  InventoryStock save(InventoryStock inventoryStock);
}
