/* (C) 2026 */

package aros.services.rms.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.service.TransferInventoryService;
import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.domain.StorageLocation;
import aros.services.rms.core.inventory.port.input.TransferInventoryUseCase.TransferItem;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferInventoryServiceTest {

  @Mock private SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  @Mock private InventoryStockRepositoryPort inventoryStockRepositoryPort;
  @Mock private InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  @Mock private StorageLocationRepositoryPort storageLocationRepositoryPort;

  private TransferInventoryService service;

  // IDs fijos para Bodega y Cocina
  private static final Long BODEGA_ID = 1L;
  private static final Long COCINA_ID = 2L;
  private static final Long VARIANT_ID = 10L;

  @BeforeEach
  void setUp() {
    service =
        new TransferInventoryService(
            supplyVariantRepositoryPort,
            inventoryStockRepositoryPort,
            inventoryMovementRepositoryPort,
            storageLocationRepositoryPort);

    // Configuración base: Bodega y Cocina siempre resueltos
    when(storageLocationRepositoryPort.findByName("Bodega"))
        .thenReturn(Optional.of(StorageLocation.builder().id(BODEGA_ID).name("Bodega").build()));
    when(storageLocationRepositoryPort.findByName("Cocina"))
        .thenReturn(Optional.of(StorageLocation.builder().id(COCINA_ID).name("Cocina").build()));
  }

  // ---------------------------------------------------------------------------
  // U-I-07: shouldDeductBodegaAndAddCocina_whenTransferIsValid
  // ---------------------------------------------------------------------------

  @Test
  void shouldDeductBodegaAndAddCocina_whenTransferIsValid() {
    // Arrange: Bodega con 10 unidades, Cocina con 0. Transferencia de 5.
    InventoryStock bodegaStock =
        InventoryStock.builder()
            .id(1L)
            .supplyVariantId(VARIANT_ID)
            .storageLocationId(BODEGA_ID)
            .currentQuantity(new BigDecimal("10"))
            .build();

    InventoryStock cocinaStock =
        InventoryStock.builder()
            .id(2L)
            .supplyVariantId(VARIANT_ID)
            .storageLocationId(COCINA_ID)
            .currentQuantity(BigDecimal.ZERO)
            .build();

    when(supplyVariantRepositoryPort.existsById(VARIANT_ID)).thenReturn(true);
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(VARIANT_ID, BODEGA_ID))
        .thenReturn(Optional.of(bodegaStock));
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(VARIANT_ID, COCINA_ID))
        .thenReturn(Optional.of(cocinaStock));
    when(inventoryStockRepositoryPort.save(any(InventoryStock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(inventoryMovementRepositoryPort.save(any(InventoryMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    service.transferToKitchen(List.of(new TransferItem(VARIANT_ID, new BigDecimal("5"))));

    // Assert: capturar los dos save() de stock
    ArgumentCaptor<InventoryStock> stockCaptor = ArgumentCaptor.forClass(InventoryStock.class);
    verify(inventoryStockRepositoryPort, times(2)).save(stockCaptor.capture());

    List<InventoryStock> savedStocks = stockCaptor.getAllValues();

    InventoryStock savedBodega =
        savedStocks.stream()
            .filter(s -> s.getStorageLocationId().equals(BODEGA_ID))
            .findFirst()
            .orElseThrow();

    InventoryStock savedCocina =
        savedStocks.stream()
            .filter(s -> s.getStorageLocationId().equals(COCINA_ID))
            .findFirst()
            .orElseThrow();

    // Bodega: 10 - 5 = 5
    assertEquals(new BigDecimal("5"), savedBodega.getCurrentQuantity());
    // Cocina: 0 + 5 = 5
    assertEquals(new BigDecimal("5"), savedCocina.getCurrentQuantity());
  }

  // ---------------------------------------------------------------------------
  // U-I-08: shouldRegisterTransferMovement_whenTransferIsValid
  // ---------------------------------------------------------------------------

  @Test
  void shouldRegisterTransferMovement_whenTransferIsValid() {
    // Arrange
    InventoryStock bodegaStock =
        InventoryStock.builder()
            .id(1L)
            .supplyVariantId(VARIANT_ID)
            .storageLocationId(BODEGA_ID)
            .currentQuantity(new BigDecimal("10"))
            .build();

    InventoryStock cocinaStock =
        InventoryStock.builder()
            .id(2L)
            .supplyVariantId(VARIANT_ID)
            .storageLocationId(COCINA_ID)
            .currentQuantity(BigDecimal.ZERO)
            .build();

    when(supplyVariantRepositoryPort.existsById(VARIANT_ID)).thenReturn(true);
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(VARIANT_ID, BODEGA_ID))
        .thenReturn(Optional.of(bodegaStock));
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(VARIANT_ID, COCINA_ID))
        .thenReturn(Optional.of(cocinaStock));
    when(inventoryStockRepositoryPort.save(any(InventoryStock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(inventoryMovementRepositoryPort.save(any(InventoryMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    service.transferToKitchen(List.of(new TransferItem(VARIANT_ID, new BigDecimal("5"))));

    // Assert: capturar el movimiento guardado
    ArgumentCaptor<InventoryMovement> movementCaptor =
        ArgumentCaptor.forClass(InventoryMovement.class);
    verify(inventoryMovementRepositoryPort, times(1)).save(movementCaptor.capture());

    InventoryMovement savedMovement = movementCaptor.getValue();
    assertEquals(MovementType.TRANSFER, savedMovement.getMovementType());
    assertEquals(BODEGA_ID, savedMovement.getFromStorageLocationId());
    assertEquals(COCINA_ID, savedMovement.getToStorageLocationId());
    assertEquals(VARIANT_ID, savedMovement.getSupplyVariantId());
    assertEquals(new BigDecimal("5"), savedMovement.getQuantity());
  }

  // ---------------------------------------------------------------------------
  // U-I-09: shouldThrowInsufficientStockException_whenBodegaHasNotEnoughStock
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowInsufficientStockException_whenBodegaHasNotEnoughStock() {
    // Arrange: Bodega con solo 2 unidades, se solicitan 5
    InventoryStock bodegaStock =
        InventoryStock.builder()
            .id(1L)
            .supplyVariantId(VARIANT_ID)
            .storageLocationId(BODEGA_ID)
            .currentQuantity(new BigDecimal("2"))
            .build();

    when(supplyVariantRepositoryPort.existsById(VARIANT_ID)).thenReturn(true);
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(VARIANT_ID, BODEGA_ID))
        .thenReturn(Optional.of(bodegaStock));

    // Act & Assert
    assertThrows(
        InsufficientStockException.class,
        () ->
            service.transferToKitchen(List.of(new TransferItem(VARIANT_ID, new BigDecimal("5")))));

    // No se debe guardar ningún movimiento
    verify(inventoryMovementRepositoryPort, never()).save(any(InventoryMovement.class));
    // El stock de Bodega no debe haber sido modificado
    verify(inventoryStockRepositoryPort, never()).save(any(InventoryStock.class));
  }
}
