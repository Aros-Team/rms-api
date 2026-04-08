/* (C) 2026 */
package aros.services.rms.core.purchase.port.output;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output port: persistence operations for PurchaseOrder. */
public interface PurchaseOrderRepositoryPort {

  PurchaseOrder save(PurchaseOrder order);

  Optional<PurchaseOrder> findById(Long id);

  List<PurchaseOrder> findAll();

  List<PurchaseOrder> findBySupplierId(Long supplierId);

  List<PurchaseOrder> findByPurchasedAtBetween(LocalDateTime from, LocalDateTime to);
}
