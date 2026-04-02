/* (C) 2026 */
package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import java.time.LocalDate;
import java.util.List;

/** Input port: queries purchase order history with various filters. */
public interface GetPurchaseHistoryUseCase {

  List<PurchaseOrder> findAll();

  PurchaseOrder findById(Long id);

  List<PurchaseOrder> findBySupplierId(Long supplierId);

  List<PurchaseOrder> findByDateRange(LocalDate from, LocalDate to);
}
