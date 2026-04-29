/* (C) 2026 */

package aros.services.rms.core.purchase.port.output;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output port: persistence operations for PurchaseOrder. */
public interface PurchaseOrderRepositoryPort {

  /**
   * Saves a purchase order.
   *
   * @param order the order to save
   * @return the saved order
   */
  PurchaseOrder save(PurchaseOrder order);

  /**
   * Finds a purchase order by ID.
   *
   * @param id the order ID
   * @return optional purchase order
   */
  Optional<PurchaseOrder> findById(Long id);

  /**
   * Returns all purchase orders.
   *
   * @return list of all orders
   */
  List<PurchaseOrder> findAll();

  /**
   * Finds orders by supplier ID.
   *
   * @param supplierId the supplier ID
   * @return list of orders
   */
  List<PurchaseOrder> findBySupplierId(Long supplierId);

  /**
   * Finds orders purchased within a date range.
   *
   * @param from start datetime
   * @param to end datetime
   * @return list of orders in range
   */
  List<PurchaseOrder> findByPurchasedAtBetween(LocalDateTime from, LocalDateTime to);
}
