/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.config;

import aros.services.rms.core.inventory.application.usecases.InventoryMovementUseCaseImpl;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.order.domain.OrderDetail;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infrastructure service that wraps InventoryMovementUseCaseImpl with Spring @Transactional.
 *
 * <p>The core use case is framework-agnostic and has no transaction annotations. This service is
 * the single transactional entry point for inventory movement operations. It also implements the
 * InventoryMovementUseCase port so it can be injected wherever the port is required (e.g.
 * TakeOrderUseCaseImpl, UpdateOrderUseCaseImpl).
 *
 * <p>Marked @Primary so Spring resolves this bean when multiple InventoryMovementUseCase
 * implementations exist in the context.
 */
@Service
@Primary
public class InventoryMovementService implements InventoryMovementUseCase {

  private final InventoryMovementUseCaseImpl delegate;

  public InventoryMovementService(
      @Qualifier("inventoryMovementUseCaseImpl") InventoryMovementUseCaseImpl delegate) {
    this.delegate = delegate;
  }

  /**
   * Deducts inventory for a completed order within a single transaction.
   *
   * @param orderId sales order reference
   * @param details order line items
   */
  @Override
  @Transactional
  public void deductForOrder(Long orderId, List<OrderDetail> details) {
    delegate.deductForOrder(orderId, details);
  }

  /**
   * Reverts inventory deductions for a cancelled order within a single transaction.
   *
   * @param orderId sales order reference
   * @param details order line items to revert
   */
  @Override
  @Transactional
  public void revertDeductionsForOrder(Long orderId, List<OrderDetail> details) {
    delegate.revertDeductionsForOrder(orderId, details);
  }
}
