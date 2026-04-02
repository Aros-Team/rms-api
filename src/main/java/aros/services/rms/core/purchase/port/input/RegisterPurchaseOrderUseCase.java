/* (C) 2026 */
package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.PurchaseOrder;

/** Input port: registers a new purchase order and triggers inventory entry movements. */
public interface RegisterPurchaseOrderUseCase {

  PurchaseOrder register(PurchaseOrder order);
}
