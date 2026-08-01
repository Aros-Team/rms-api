/* (C) 2026 */

package aros.services.rms.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.application.service.InventoryMovementService;
import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.StorageLocation;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.purchase.application.service.RegisterPurchaseOrderService;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.core.purchase.port.output.PurchaseOrderRepositoryPort;
import aros.services.rms.core.purchase.port.output.SupplierRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Mock private ProductOptionRepositoryPort productOptionRepositoryPort;

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
            notificationPort,
            productOptionRepositoryPort);

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
    // Cocina también (necesaria para deductForOrder)
    when(storageLocationRepositoryPort.findByName("Cocina"))
        .thenReturn(Optional.of(StorageLocation.builder().id(2L).name("Cocina").build()));
    // Stock abundante en cualquier ubicación: la deducción completa sale de Cocina
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(anyLong(), anyLong()))
        .thenReturn(
            Optional.of(
                InventoryStock.builder()
                    .id(1L)
                    .supplyVariantId(0L)
                    .storageLocationId(0L)
                    .currentQuantity(new BigDecimal("1000"))
                    .build()));
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

  // ---------------------------------------------------------------------------
  // Fase D — semántica de selección en buildRequiredVariantsMap (vía deductForOrder)
  // ---------------------------------------------------------------------------

  private static final Long PRODUCT_ID = 1L;
  private static final Long SLOT_VARIANT = 10L; // variante de la línea base reemplazable
  private static final Long OTHER_BASE_VARIANT = 11L; // otra línea base
  private static final Long OPTION_VARIANT = 12L; // variante de la receta de la opción
  private static final Long EXTRA_VARIANT = 13L;

  private List<ProductRecipe> baseRecipes() {
    return List.of(
        ProductRecipe.builder()
            .productId(PRODUCT_ID)
            .supplyVariantId(SLOT_VARIANT)
            .requiredQuantity(new BigDecimal("2"))
            .build(),
        ProductRecipe.builder()
            .productId(PRODUCT_ID)
            .supplyVariantId(OTHER_BASE_VARIANT)
            .requiredQuantity(new BigDecimal("1"))
            .build());
  }

  private ProductOption substitutionOption() {
    OptionCategory category =
        OptionCategory.builder()
            .id(7L)
            .name("Salsa")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(500L)
            .build();
    return ProductOption.builder().id(100L).name("Salsa tártara").category(category).build();
  }

  private ProductOption removeOption() {
    OptionCategory category =
        OptionCategory.builder()
            .id(8L)
            .name("Quitar")
            .selectionType(OptionSelectionType.REMOVE)
            .build();
    return ProductOption.builder().id(101L).name("Sin cebolla").category(category).build();
  }

  private ProductOption multiSelectOption() {
    OptionCategory category =
        OptionCategory.builder()
            .id(9L)
            .name("Adiciones")
            .selectionType(OptionSelectionType.MULTI_SELECT)
            .build();
    return ProductOption.builder().id(102L).name("Tocino").category(category).build();
  }

  private ProductOption extraOption() {
    OptionCategory category =
        OptionCategory.builder()
            .id(10L)
            .name("Extra")
            .selectionType(OptionSelectionType.EXTRA)
            .build();
    return ProductOption.builder().id(104L).name("Extra queso").category(category).build();
  }

  private ProductOption singleChoiceOption() {
    OptionCategory category =
        OptionCategory.builder()
            .id(11L)
            .name("Cocción")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .build();
    return ProductOption.builder().id(103L).name("Término medio").category(category).build();
  }

  private void stubOptionRecipe(Long optionId, Long variantId, BigDecimal quantity) {
    when(optionRecipeRepositoryPort.findByOptionIdIn(List.of(optionId)))
        .thenReturn(
            List.of(
                OptionRecipe.builder()
                    .optionId(optionId)
                    .supplyVariantId(variantId)
                    .requiredQuantity(quantity)
                    .build()));
  }

  private OrderDetail detailWithOptions(List<ProductOption> options) {
    return OrderDetail.builder()
        .product(Product.builder().id(PRODUCT_ID).name("P1").build())
        .selectedOptions(options)
        .build();
  }

  private Map<Long, BigDecimal> captureDeductedVariants() {
    ArgumentCaptor<InventoryMovement> captor = ArgumentCaptor.forClass(InventoryMovement.class);
    verify(inventoryMovementRepositoryPort, atLeastOnce()).save(captor.capture());
    Map<Long, BigDecimal> deducted = new HashMap<>();
    for (InventoryMovement movement : captor.getAllValues()) {
      deducted.merge(movement.getSupplyVariantId(), movement.getQuantity(), BigDecimal::add);
    }
    return deducted;
  }

  @Test
  void shouldDeductWithoutBaseSlotVariant_whenSubstitutionSelected() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    when(productOptionRepositoryPort.loadBaseRecipeBySupplyCategory(PRODUCT_ID, 500L))
        .thenReturn(
            List.of(
                ProductRecipe.builder()
                    .productId(PRODUCT_ID)
                    .supplyVariantId(SLOT_VARIANT)
                    .requiredQuantity(new BigDecimal("2"))
                    .build()));
    stubOptionRecipe(100L, OPTION_VARIANT, new BigDecimal("3"));

    inventoryMovementHelper.deductForOrder(
        1L, List.of(detailWithOptions(List.of(substitutionOption()))));

    Map<Long, BigDecimal> deducted = captureDeductedVariants();
    // La línea base del slot (variante 10) queda fuera; se deduce la receta de la opción.
    assertEquals(2, deducted.size());
    assertEquals(0, deducted.get(OTHER_BASE_VARIANT).compareTo(BigDecimal.ONE));
    assertEquals(0, deducted.get(OPTION_VARIANT).compareTo(new BigDecimal("3")));
  }

  @Test
  void shouldDeductBaseRecipe_whenNoSelection() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());

    inventoryMovementHelper.deductForOrder(1L, List.of(detailWithOptions(List.of())));

    Map<Long, BigDecimal> deducted = captureDeductedVariants();
    // Base intacta: ambas líneas se deducen.
    assertEquals(2, deducted.size());
    assertEquals(0, deducted.get(SLOT_VARIANT).compareTo(new BigDecimal("2")));
    assertEquals(0, deducted.get(OTHER_BASE_VARIANT).compareTo(BigDecimal.ONE));
  }

  @Test
  void shouldSkipCancelledBaseLine_whenRemoveSelected() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(101L, SLOT_VARIANT, new BigDecimal("2")); // cancela la línea del slot

    inventoryMovementHelper.deductForOrder(1L, List.of(detailWithOptions(List.of(removeOption()))));

    Map<Long, BigDecimal> deducted = captureDeductedVariants();
    // La variante 10 queda en neto 0 y no se deduce; la 11 permanece.
    assertEquals(1, deducted.size());
    assertEquals(0, deducted.get(OTHER_BASE_VARIANT).compareTo(BigDecimal.ONE));
  }

  @Test
  void shouldNotRegisterMovements_whenRemoveCancelsAllBaseLines() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    when(optionRecipeRepositoryPort.findByOptionIdIn(List.of(101L)))
        .thenReturn(
            List.of(
                OptionRecipe.builder()
                    .optionId(101L)
                    .supplyVariantId(SLOT_VARIANT)
                    .requiredQuantity(new BigDecimal("2"))
                    .build(),
                OptionRecipe.builder()
                    .optionId(101L)
                    .supplyVariantId(OTHER_BASE_VARIANT)
                    .requiredQuantity(new BigDecimal("1"))
                    .build()));

    inventoryMovementHelper.deductForOrder(1L, List.of(detailWithOptions(List.of(removeOption()))));

    verify(inventoryMovementRepositoryPort, never()).save(any());
  }

  @Test
  void shouldDeductBasePlusOptionRecipes_whenMultiAndExtraSelected() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(102L, OPTION_VARIANT, new BigDecimal("3"));
    stubOptionRecipe(104L, EXTRA_VARIANT, new BigDecimal("1"));

    inventoryMovementHelper.deductForOrder(
        1L, List.of(detailWithOptions(List.of(multiSelectOption(), extraOption()))));

    Map<Long, BigDecimal> deducted = captureDeductedVariants();
    // Base + recetas de las opciones (comportamiento actual).
    assertEquals(4, deducted.size());
    assertEquals(0, deducted.get(SLOT_VARIANT).compareTo(new BigDecimal("2")));
    assertEquals(0, deducted.get(OTHER_BASE_VARIANT).compareTo(BigDecimal.ONE));
    assertEquals(0, deducted.get(OPTION_VARIANT).compareTo(new BigDecimal("3")));
    assertEquals(0, deducted.get(EXTRA_VARIANT).compareTo(BigDecimal.ONE));
  }

  @Test
  void shouldDeductBasePlusOptionRecipe_whenSingleChoiceWithoutReplacement() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(103L, OPTION_VARIANT, new BigDecimal("3"));

    inventoryMovementHelper.deductForOrder(
        1L, List.of(detailWithOptions(List.of(singleChoiceOption()))));

    Map<Long, BigDecimal> deducted = captureDeductedVariants();
    assertEquals(3, deducted.size());
    assertEquals(0, deducted.get(SLOT_VARIANT).compareTo(new BigDecimal("2")));
    assertEquals(0, deducted.get(OTHER_BASE_VARIANT).compareTo(BigDecimal.ONE));
    assertEquals(0, deducted.get(OPTION_VARIANT).compareTo(new BigDecimal("3")));
  }
}
