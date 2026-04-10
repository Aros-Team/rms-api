/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.inventory.application.service.InventoryMovementService;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.purchase.application.service.CreateSupplierService;
import aros.services.rms.core.purchase.application.service.GetPurchaseHistoryService;
import aros.services.rms.core.purchase.application.service.RegisterPurchaseOrderService;
import aros.services.rms.core.purchase.port.input.CreateSupplierUseCase;
import aros.services.rms.core.purchase.port.input.GetPurchaseHistoryUseCase;
import aros.services.rms.core.purchase.port.input.GetSuppliersUseCase;
import aros.services.rms.core.purchase.port.input.RegisterPurchaseOrderUseCase;
import aros.services.rms.core.purchase.port.input.UpdateSupplierUseCase;
import aros.services.rms.core.purchase.port.output.PurchaseOrderRepositoryPort;
import aros.services.rms.core.purchase.port.output.SupplierRepositoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring bean configuration for the purchase module.
 *
 * <p>Supplier operations are exposed through three separate beans, one per use case interface. Each
 * bean returns the same CreateSupplierService instance since it implements all three.
 */
@Configuration
public class PurchaseConfigBeans {

  private CreateSupplierService createSupplierServiceInstance(
      SupplierRepositoryPort supplierRepositoryPort, Logger logger) {
    return new CreateSupplierService(supplierRepositoryPort, logger);
  }

  @Bean("createSupplierUseCase")
  public CreateSupplierUseCase createSupplierUseCase(
      SupplierRepositoryPort supplierRepositoryPort, Logger logger) {
    return createSupplierServiceInstance(supplierRepositoryPort, logger);
  }

  @Bean("updateSupplierUseCase")
  public UpdateSupplierUseCase updateSupplierUseCase(
      SupplierRepositoryPort supplierRepositoryPort, Logger logger) {
    return createSupplierServiceInstance(supplierRepositoryPort, logger);
  }

  @Bean("getSuppliersUseCase")
  public GetSuppliersUseCase getSuppliersUseCase(
      SupplierRepositoryPort supplierRepositoryPort, Logger logger) {
    return createSupplierServiceInstance(supplierRepositoryPort, logger);
  }

  /**
   * Registers the purchase order registration use case. Injects InventoryMovementUseCaseImpl
   * directly by qualifier to reuse its stock/movement helpers inside the same transaction managed
   * by RegisterPurchaseOrderService.
   */
  @Bean
  public RegisterPurchaseOrderUseCase registerPurchaseOrderUseCase(
      SupplierRepositoryPort supplierRepositoryPort,
      PurchaseOrderRepositoryPort purchaseOrderRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      @Qualifier("inventoryMovementUseCaseImpl") InventoryMovementService inventoryMovementHelper,
      Logger logger) {
    return new RegisterPurchaseOrderService(
        supplierRepositoryPort,
        purchaseOrderRepositoryPort,
        supplyVariantRepositoryPort,
        storageLocationRepositoryPort,
        inventoryStockRepositoryPort,
        inventoryMovementRepositoryPort,
        inventoryMovementHelper,
        logger);
  }

  /** Registers the purchase history query use case. */
  @Bean
  public GetPurchaseHistoryUseCase getPurchaseHistoryUseCase(
      PurchaseOrderRepositoryPort purchaseOrderRepositoryPort,
      SupplierRepositoryPort supplierRepositoryPort) {
    return new GetPurchaseHistoryService(purchaseOrderRepositoryPort, supplierRepositoryPort);
  }
}
