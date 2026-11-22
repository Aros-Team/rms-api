/* (C) 2026 */

package aros.services.rms.core.purchase.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.application.service.InventoryMovementService;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.purchase.application.exception.InvalidPurchaseItemException;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
import aros.services.rms.core.purchase.domain.PurchaseStatus;
import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.core.purchase.port.output.PurchaseOrderRepositoryPort;
import aros.services.rms.core.purchase.port.output.SupplierRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link RegisterPurchaseOrderService}: status and total derivation.
 *
 * <p><b>Feature:</b> Registering a purchase order must derive its lifecycle status and total from
 * the line items on the server, not trust the request header.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>At least one received line persists RECEIVED
 *   <li>No received lines persists CONFIRMED and creates no stock movement
 *   <li>The stored total is the sum of quantityOrdered * unitPrice, ignoring a bogus client total
 *   <li>quantityReceived above quantityOrdered is rejected
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the supplier, purchase, inventory and logger ports
 *   <li>Captures the domain object handed to the purchase repository and asserts its status/total
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RegisterPurchaseOrderServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private SupplierRepositoryPort supplierRepositoryPort;
  @Mock private PurchaseOrderRepositoryPort purchaseOrderRepositoryPort;
  @Mock private SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  @Mock private StorageLocationRepositoryPort storageLocationRepositoryPort;
  @Mock private InventoryStockRepositoryPort inventoryStockRepositoryPort;
  @Mock private InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  @Mock private InventoryMovementService inventoryMovementHelper;
  @Mock private Logger logger;

  private RegisterPurchaseOrderService service;

  @BeforeEach
  void setUp() {
    service =
        new RegisterPurchaseOrderService(
            supplierRepositoryPort,
            purchaseOrderRepositoryPort,
            supplyVariantRepositoryPort,
            storageLocationRepositoryPort,
            inventoryStockRepositoryPort,
            inventoryMovementRepositoryPort,
            inventoryMovementHelper,
            logger);
  }

  @Test
  void should_derive_received_status_and_server_side_total() {
    stubValidSupplierAndVariants();
    when(purchaseOrderRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

    PurchaseOrder saved =
        service.register(
            order(
                new Money(new BigDecimal("1.00"), COP),
                line(10L, "10.000", "4.000", "1500.00"),
                line(11L, "2.000", "0.000", "2000.00")));

    assertEquals(PurchaseStatus.RECEIVED, saved.getStatus());
    assertEquals(new BigDecimal("19000.00"), saved.getTotalAmount().amount());
    verify(inventoryMovementHelper).addStockToLocation(10L, 1L, new BigDecimal("4.000"));
    verify(inventoryMovementHelper, never()).addStockToLocation(eq(11L), anyLong(), any());
  }

  @Test
  void should_derive_confirmed_status_when_nothing_arrived() {
    stubValidSupplierAndVariants();
    when(purchaseOrderRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

    PurchaseOrder saved =
        service.register(
            order(new Money(new BigDecimal("999.00"), COP), line(10L, "3.000", "0.000", "500.00")));

    assertEquals(PurchaseStatus.CONFIRMED, saved.getStatus());
    assertEquals(new BigDecimal("1500.00"), saved.getTotalAmount().amount());
    verify(inventoryMovementHelper, never()).addStockToLocation(anyLong(), anyLong(), any());
  }

  @Test
  void should_reject_received_quantity_above_ordered() {
    when(supplierRepositoryPort.findById(1L))
        .thenReturn(Optional.of(Supplier.builder().id(1L).name("Proveedor").active(true).build()));
    when(supplyVariantRepositoryPort.existsById(anyLong())).thenReturn(true);

    assertThrows(
        InvalidPurchaseItemException.class,
        () ->
            service.register(
                order(new Money(BigDecimal.ZERO, COP), line(10L, "2.000", "3.000", "100.00"))));

    verify(purchaseOrderRepositoryPort, never()).save(any());
  }

  private void stubValidSupplierAndVariants() {
    when(supplierRepositoryPort.findById(1L))
        .thenReturn(Optional.of(Supplier.builder().id(1L).name("Proveedor").active(true).build()));
    when(supplyVariantRepositoryPort.existsById(anyLong())).thenReturn(true);
    when(storageLocationRepositoryPort.findByName("Bodega"))
        .thenReturn(
            Optional.of(
                aros.services.rms.core.inventory.domain.StorageLocation.builder()
                    .id(1L)
                    .name("Bodega")
                    .build()));
  }

  private static PurchaseOrder order(Money total, PurchaseOrderItem... items) {
    return PurchaseOrder.builder()
        .supplierId(1L)
        .registeredById(2L)
        .purchasedAt(LocalDateTime.of(2026, 11, 20, 7, 30))
        .totalAmount(total)
        .notes("test")
        .items(List.of(items))
        .build();
  }

  private static PurchaseOrderItem line(
      Long variantId, String ordered, String received, String unitPrice) {
    return PurchaseOrderItem.builder()
        .supplyVariantId(variantId)
        .quantityOrdered(new BigDecimal(ordered))
        .quantityReceived(new BigDecimal(received))
        .unitPrice(new Money(new BigDecimal(unitPrice), COP))
        .build();
  }
}
