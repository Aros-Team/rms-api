/* (C) 2026 */
package aros.services.rms.core.purchase.application.usecases;

import aros.services.rms.core.purchase.application.exception.SupplierNotFoundException;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.port.input.GetPurchaseHistoryUseCase;
import aros.services.rms.core.purchase.port.output.PurchaseOrderRepositoryPort;
import aros.services.rms.core.purchase.port.output.SupplierRepositoryPort;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Pure business logic for querying purchase order history.
 *
 * <p>Framework-agnostic: no Spring annotations. Supports filtering by supplier and date range.
 */
public class GetPurchaseHistoryUseCaseImpl implements GetPurchaseHistoryUseCase {

  private final PurchaseOrderRepositoryPort purchaseOrderRepositoryPort;
  private final SupplierRepositoryPort supplierRepositoryPort;

  public GetPurchaseHistoryUseCaseImpl(
      PurchaseOrderRepositoryPort purchaseOrderRepositoryPort,
      SupplierRepositoryPort supplierRepositoryPort) {
    this.purchaseOrderRepositoryPort = purchaseOrderRepositoryPort;
    this.supplierRepositoryPort = supplierRepositoryPort;
  }

  /**
   * Returns all purchase orders ordered by creation date descending.
   *
   * @return list of all purchase orders
   */
  @Override
  public List<PurchaseOrder> findAll() {
    return purchaseOrderRepositoryPort.findAll();
  }

  /**
   * Returns a single purchase order by its identifier.
   *
   * @param id purchase order identifier
   * @return the purchase order
   * @throws aros.services.rms.core.purchase.application.exception.SupplierNotFoundException if not
   *     found (reuses existing 404 pattern — a dedicated PurchaseOrderNotFoundException can be
   *     added later)
   */
  @Override
  public PurchaseOrder findById(Long id) {
    return purchaseOrderRepositoryPort
        .findById(id)
        .orElseThrow(
            () ->
                new aros.services.rms.core.purchase.application.exception
                    .PurchaseOrderNotFoundException(id));
  }

  /**
   * Returns all purchase orders for a given supplier.
   *
   * @param supplierId supplier identifier
   * @return list of purchase orders for the supplier
   * @throws SupplierNotFoundException if the supplier does not exist
   */
  @Override
  public List<PurchaseOrder> findBySupplierId(Long supplierId) {
    // Validate supplier exists before querying orders
    supplierRepositoryPort
        .findById(supplierId)
        .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    return purchaseOrderRepositoryPort.findBySupplierId(supplierId);
  }

  /**
   * Returns all purchase orders whose purchasedAt falls within the given date range (inclusive).
   *
   * @param from start date (inclusive), converted to start of day
   * @param to end date (inclusive), converted to end of day
   * @return list of purchase orders in the range
   */
  @Override
  public List<PurchaseOrder> findByDateRange(LocalDate from, LocalDate to) {
    return purchaseOrderRepositoryPort.findByPurchasedAtBetween(
        from.atStartOfDay(), to.atTime(LocalTime.MAX));
  }
}
