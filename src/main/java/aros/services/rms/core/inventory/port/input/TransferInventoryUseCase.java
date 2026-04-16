/* (C) 2026 */
package aros.services.rms.core.inventory.port.input;

import aros.services.rms.core.inventory.domain.InventoryMovement;
import java.math.BigDecimal;
import java.util.List;

/** Input port for inventory transfer operations between storage locations. */
public interface TransferInventoryUseCase {

  /**
   * Transfers multiple supply variants from Bodega to Cocina.
   *
   * @param items list of transfer items containing variant IDs and quantities
   * @return list of registered inventory movements
   */
  List<InventoryMovement> transferToKitchen(List<TransferItem> items);

  /** Transfer item containing supply variant ID and quantity to transfer. */
  record TransferItem(Long supplyVariantId, BigDecimal quantity) {}
}
