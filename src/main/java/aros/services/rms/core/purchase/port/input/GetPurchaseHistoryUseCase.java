/* (C) 2026 */

package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import java.time.LocalDate;
import java.util.List;

/** Input port: queries purchase order history with various filters. */
public interface GetPurchaseHistoryUseCase {

  /**
   * Returns all purchase orders.
   *
   * @return list of all purchase orders
   */
  List<PurchaseOrder> findAll();

  /**
   * Finds a purchase order by ID.
   *
   * @param id purchase order ID
   * @return the purchase order
   */
  PurchaseOrder findById(Long id);

  /**
   * Finds purchase orders by supplier ID.
   *
   * @param supplierId the supplier ID
   * @return list of purchase orders
   */
  List<PurchaseOrder> findBySupplierId(Long supplierId);

  /**
   * Finds purchase orders by date range.
   *
   * @param from start date
   * @param to end date
   * @return list of purchase orders in range
   */
  List<PurchaseOrder> findByDateRange(LocalDate from, LocalDate to);
}
