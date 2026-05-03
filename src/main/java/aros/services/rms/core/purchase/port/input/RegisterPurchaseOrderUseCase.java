/* (C) 2026 */

package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.PurchaseOrder;

/** Input port: registers a new purchase order and triggers inventory entry movements. */
public interface RegisterPurchaseOrderUseCase {

  /**
   * Registers a new purchase order.
   *
   * @param order the purchase order to register
   * @return the registered purchase order
   */
  PurchaseOrder register(PurchaseOrder order);
}
