/* (C) 2026 */

package aros.services.rms.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.application.service.InventoryMovementService;
import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.domain.StorageLocation;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.purchase.application.service.RegisterPurchaseOrderService;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryMovementServiceTest {

  // --- Ports mockeados para InventoryMovementService (helper) ---
  @Mock
  private aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort
      productRecipeRepositoryPort;

  @Mock
  private aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort
      optionRecipeRepositoryPort;

  @Mock private InventoryStockRepositoryPort inventoryStockRepositoryPort;
  @Mock private InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  @Mock private StorageLocationRepositoryPort storageLocationRepositoryPort;
  @Mock private aros.services.rms.core.common.metrics.BusinessMetricsPort metricsPort;

  @Mock
  private aros.services.rms.core.common.notification.port.output.NotificationPort notificationPort;

  // --- Ports mockeados para RegisterPurchaseOrderService ---
  @Mock private SupplierRepositoryPort supplierRepositoryPort;
  @Mock private PurchaseOrderRepositoryPort purchaseOrderRepositoryPort;
  @Mock private SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  @Mock private Logger logger;

  private InventoryMovementService inventoryMovementHelper;
  private RegisterPurchaseOrderService registerPurchaseOrderService;

  private static final Long BODEGA_ID = 1L;
  private static final Long VARIANT_ID = 10L;
  private static final Long SUPPLIER_ID = 5L;
  private static final Long PURCHASE_ORDER_ID = 99L;

  @BeforeEach
  void setUp() {
    // Construir el helper de movimientos de inventario
    inventoryMovementHelper =
        new InventoryMovementService(
            productRecipeRepositoryPort,
            optionRecipeRepositoryPort,
            inventoryStockRepositoryPort,
            inventoryMovementRepositoryPort,
            storageLocationRepositoryPort,
            metricsPort,
            notificationPort);

    // Construir el servicio de registro de compras
    registerPurchaseOrderService =
        new RegisterPurchaseOrderService(
            supplierRepositoryPort,
            purchaseOrderRepositoryPort,
            supplyVariantRepositoryPort,
            storageLocationRepositoryPort,
            inventoryStockRepositoryPort,
            inventoryMovementRepositoryPort,
            inventoryMovementHelper,
            logger);

    // Bodega siempre resuelta
    when(storageLocationRepositoryPort.findByName("Bodega"))
        .thenReturn(Optional.of(StorageLocation.builder().id(BODEGA_ID).name("Bodega").build()));
  }

  // ---------------------------------------------------------------------------
  // U-I-10: shouldAddStockToBodega_whenPurchaseIsRegistered
  // ---------------------------------------------------------------------------

  @Test
  void shouldAddStockToBodega_whenPurchaseIsRegistered() {
    // Arrange: proveedor activo, variante existente, stock Bodega en 0
    Supplier supplier = Supplier.builder().id(SUPPLIER_ID).name("Proveedor A").active(true).build();

    PurchaseOrderItem item =
        PurchaseOrderItem.builder()
            .supplyVariantId(VARIANT_ID)
            .quantityOrdered(new BigDecimal("10"))
            .quantityReceived(new BigDecimal("10"))
            .unitPrice(new Money(new BigDecimal("2.50"), Currency.getInstance("COP")))
            .build();

    PurchaseOrder order =
        PurchaseOrder.builder()
            .supplierId(SUPPLIER_ID)
            .registeredById(1L)
            .purchasedAt(LocalDateTime.now())
            .totalAmount(new Money(new BigDecimal("25.00"), Currency.getInstance("COP")))
            .items(List.of(item))
            .build();

    PurchaseOrder savedOrder =
        PurchaseOrder.builder()
            .id(PURCHASE_ORDER_ID)
            .supplierId(SUPPLIER_ID)
            .registeredById(1L)
            .purchasedAt(order.getPurchasedAt())
            .totalAmount(order.getTotalAmount())
            .items(List.of(item))
            .build();

    InventoryStock bodegaStock =
        InventoryStock.builder()
            .id(1L)
            .supplyVariantId(VARIANT_ID)
            .storageLocationId(BODEGA_ID)
            .currentQuantity(BigDecimal.ZERO)
            .build();

    when(supplierRepositoryPort.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier));
    when(supplyVariantRepositoryPort.existsById(VARIANT_ID)).thenReturn(true);
    when(purchaseOrderRepositoryPort.save(any(PurchaseOrder.class))).thenReturn(savedOrder);
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(VARIANT_ID, BODEGA_ID))
        .thenReturn(Optional.of(bodegaStock));
    when(inventoryStockRepositoryPort.save(any(InventoryStock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(inventoryMovementRepositoryPort.save(any(InventoryMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    registerPurchaseOrderService.register(order);

    // Assert: capturar el stock guardado en Bodega
    ArgumentCaptor<InventoryStock> stockCaptor = ArgumentCaptor.forClass(InventoryStock.class);
    verify(inventoryStockRepositoryPort, times(1)).save(stockCaptor.capture());

    InventoryStock savedStock = stockCaptor.getValue();
    assertEquals(BODEGA_ID, savedStock.getStorageLocationId());
    // Bodega: 0 + 10 = 10
    assertEquals(new BigDecimal("10"), savedStock.getCurrentQuantity());
  }

  // ---------------------------------------------------------------------------
  // U-I-11: shouldRegisterEntryMovement_whenPurchaseIsRegistered
  // ---------------------------------------------------------------------------

  @Test
  void shouldRegisterEntryMovement_whenPurchaseIsRegistered() {
    // Arrange: mismo flujo exitoso
    Supplier supplier = Supplier.builder().id(SUPPLIER_ID).name("Proveedor A").active(true).build();

    PurchaseOrderItem item =
        PurchaseOrderItem.builder()
            .supplyVariantId(VARIANT_ID)
            .quantityOrdered(new BigDecimal("10"))
            .quantityReceived(new BigDecimal("10"))
            .unitPrice(new Money(new BigDecimal("2.50"), Currency.getInstance("COP")))
            .build();

    PurchaseOrder order =
        PurchaseOrder.builder()
            .supplierId(SUPPLIER_ID)
            .registeredById(1L)
            .purchasedAt(LocalDateTime.now())
            .totalAmount(new Money(new BigDecimal("25.00"), Currency.getInstance("COP")))
            .items(List.of(item))
            .build();

    PurchaseOrder savedOrder =
        PurchaseOrder.builder()
            .id(PURCHASE_ORDER_ID)
            .supplierId(SUPPLIER_ID)
            .registeredById(1L)
            .purchasedAt(order.getPurchasedAt())
            .totalAmount(order.getTotalAmount())
            .items(List.of(item))
            .build();

    InventoryStock bodegaStock =
        InventoryStock.builder()
            .id(1L)
            .supplyVariantId(VARIANT_ID)
            .storageLocationId(BODEGA_ID)
            .currentQuantity(BigDecimal.ZERO)
            .build();

    when(supplierRepositoryPort.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier));
    when(supplyVariantRepositoryPort.existsById(VARIANT_ID)).thenReturn(true);
    when(purchaseOrderRepositoryPort.save(any(PurchaseOrder.class))).thenReturn(savedOrder);
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(VARIANT_ID, BODEGA_ID))
        .thenReturn(Optional.of(bodegaStock));
    when(inventoryStockRepositoryPort.save(any(InventoryStock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(inventoryMovementRepositoryPort.save(any(InventoryMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    registerPurchaseOrderService.register(order);

    // Assert: capturar el movimiento registrado
    ArgumentCaptor<InventoryMovement> movementCaptor =
        ArgumentCaptor.forClass(InventoryMovement.class);
    verify(inventoryMovementRepositoryPort, times(1)).save(movementCaptor.capture());

    InventoryMovement savedMovement = movementCaptor.getValue();
    // Tipo ENTRY
    assertEquals(MovementType.ENTRY, savedMovement.getMovementType());
    // toLocation = Bodega
    assertEquals(BODEGA_ID, savedMovement.getToStorageLocationId());
    // fromLocation = null (entrada externa)
    assertEquals(null, savedMovement.getFromStorageLocationId());
    // Referencia a la purchase order correcta
    assertEquals(PURCHASE_ORDER_ID, savedMovement.getReferencePurchaseOrderId());
    // Sin referencia a orden de venta
    assertEquals(null, savedMovement.getReferenceOrderId());
    // Cantidad correcta
    assertEquals(new BigDecimal("10"), savedMovement.getQuantity());
  }
}
