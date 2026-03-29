/* (C) 2026 */
package aros.services.rms.core.inventory.port.input;

import aros.services.rms.core.order.domain.OrderDetail;
import java.util.List;

/** Input port for inventory movement operations. */
public interface InventoryMovementUseCase {

  /**
   * Deducts inventory stock for an order. Deducts from Cocina first, then Bodega as fallback.
   *
   * @param orderId the order id
   * @param details the order details
   */
  void deductForOrder(Long orderId, List<OrderDetail> details);

  /**
   * Reverts inventory deductions for an order. Returns stock to original locations.
   *
   * @param orderId the order id
   * @param details the order details to revert
   */
  void revertDeductionsForOrder(Long orderId, List<OrderDetail> details);
}
