/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.config;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.port.input.RegisterPurchaseOrderUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infrastructure service that wraps RegisterPurchaseOrderUseCase with Spring @Transactional.
 *
 * <p>Ensures that persisting the purchase order, updating inventory stock and registering inventory
 * movements all happen within a single atomic transaction. If any step fails, the entire operation
 * is rolled back.
 */
@Service
public class RegisterPurchaseOrderService {

  private final RegisterPurchaseOrderUseCase registerPurchaseOrderUseCase;
  private final BusinessMetricsPort metricsPort;

  public RegisterPurchaseOrderService(
      RegisterPurchaseOrderUseCase registerPurchaseOrderUseCase, BusinessMetricsPort metricsPort) {
    this.registerPurchaseOrderUseCase = registerPurchaseOrderUseCase;
    this.metricsPort = metricsPort;
  }

  /**
   * Registers a purchase order atomically: persists the order, updates Bodega stock and creates
   * ENTRY movements for each received item.
   *
   * @param order domain object with all purchase data and items
   * @return the persisted purchase order with generated id and items
   */
  @Transactional
  public PurchaseOrder register(PurchaseOrder order) {
    try {
      PurchaseOrder result = registerPurchaseOrderUseCase.register(order);
      metricsPort.recordPurchaseRegistered(true);
      return result;
    } catch (Exception e) {
      metricsPort.recordPurchaseRegistered(false);
      metricsPort.recordPurchaseOrderSyncError();
      throw e;
    }
  }
}
