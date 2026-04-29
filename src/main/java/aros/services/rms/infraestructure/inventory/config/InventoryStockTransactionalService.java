/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.config;

import aros.services.rms.core.inventory.application.service.InventoryStockService;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infrastructure service that wraps InventoryStockService with Spring @Transactional.
 *
 * <p>The core use case is framework-agnostic and has no transaction annotations. This service is
 * the single transactional entry point for inventory stock availability checks. It also implements
 * the InventoryStockUseCase port so it can be injected wherever the port is required.
 *
 * <p>Marked @Primary so Spring resolves this bean when multiple InventoryStockUseCase
 * implementations exist in the context.
 *
 * <p>The @Transactional annotation is required here because the availability check uses pessimistic
 * locking (SELECT FOR UPDATE) which requires a read-write transaction context.
 */
@Service
@Primary
public class InventoryStockTransactionalService implements InventoryStockUseCase {

  private final InventoryStockService delegate;

  /**
   * Creates a new instance.
   *
   * @param delegate the inventory stock service
   */
  public InventoryStockTransactionalService(
      @Qualifier("inventoryStockUseCaseImpl") InventoryStockService delegate) {
    this.delegate = delegate;
  }

  /**
   * Checks if sufficient inventory is available for a product and its selected options.
   *
   * <p>This method uses pessimistic locking (SELECT FOR UPDATE) to prevent race conditions during
   * concurrent order creation, so it must run within a read-write transaction.
   *
   * @param productId the product to check
   * @param selectedOptionIds the selected options for the product
   * @return true if sufficient stock is available, false otherwise
   */
  @Override
  @Transactional
  public boolean isAvailable(Long productId, List<Long> selectedOptionIds) {
    return delegate.isAvailable(productId, selectedOptionIds);
  }
}
