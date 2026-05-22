/* (C) 2026 */

package aros.services.rms.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantAlreadyExistsException;
import aros.services.rms.infraestructure.inventory.api.SupplyCatalogController;
import aros.services.rms.infraestructure.inventory.api.dto.CreateSupplyRequest;
import aros.services.rms.infraestructure.inventory.api.dto.CreateSupplyVariantRequest;
import aros.services.rms.infraestructure.inventory.persistence.InventoryStockEntity;
import aros.services.rms.infraestructure.inventory.persistence.StorageLocationEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyCategoryEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import aros.services.rms.infraestructure.inventory.persistence.UnitOfMeasureEntity;
import aros.services.rms.infraestructure.inventory.persistence.jpa.InventoryStockRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.StorageLocationRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyCategoryRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.UnitOfMeasureRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SupplyCatalogServiceTest {

  @Mock private SupplyRepository supplyRepository;
  @Mock private SupplyVariantRepository supplyVariantRepository;
  @Mock private SupplyCategoryRepository supplyCategoryRepository;
  @Mock private InventoryStockRepository inventoryStockRepository;
  @Mock private StorageLocationRepository storageLocationRepository;
  @Mock private UnitOfMeasureRepository unitOfMeasureRepository;

  private SupplyCatalogController controller;

  @BeforeEach
  void setUp() {
    controller =
        new SupplyCatalogController(
            supplyRepository,
            supplyVariantRepository,
            supplyCategoryRepository,
            inventoryStockRepository,
            storageLocationRepository,
            unitOfMeasureRepository);
  }

  // ---------------------------------------------------------------------------
  // U-I-01: shouldCreateSupply_whenCategoryExists
  // ---------------------------------------------------------------------------

  @Test
  void shouldCreateSupply_whenCategoryExists() {
    // Arrange
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();
    SupplyEntity savedSupply =
        SupplyEntity.builder().id(10L).name("Pan").category(category).build();

    when(supplyCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(supplyRepository.findByNameIgnoreCase("Pan")).thenReturn(Optional.empty());
    when(supplyRepository.save(any(SupplyEntity.class))).thenReturn(savedSupply);

    CreateSupplyRequest request = new CreateSupplyRequest("Pan", 1L);

    // Act
    ResponseEntity<?> response = controller.createSupply(request);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(supplyRepository, times(1)).save(any(SupplyEntity.class));
  }

  // ---------------------------------------------------------------------------
  // U-I-02: shouldThrowException_whenSupplyNameIsDuplicated
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowException_whenSupplyNameIsDuplicated() {
    // Arrange
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();
    SupplyEntity existing = SupplyEntity.builder().id(5L).name("Pan").category(category).build();

    when(supplyCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(supplyRepository.findByNameIgnoreCase("Pan")).thenReturn(Optional.of(existing));

    CreateSupplyRequest request = new CreateSupplyRequest("Pan", 1L);

    // Act & Assert
    assertThrows(SupplyAlreadyExistsException.class, () -> controller.createSupply(request));
    verify(supplyRepository, never()).save(any(SupplyEntity.class));
  }

  // ---------------------------------------------------------------------------
  // U-I-03: shouldThrowException_whenCategoryNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowException_whenCategoryNotFound() {
    // Arrange: categoría inexistente
    when(supplyCategoryRepository.findById(99L)).thenReturn(Optional.empty());

    CreateSupplyRequest request = new CreateSupplyRequest("Pan", 99L);

    // Act & Assert
    assertThrows(StorageLocationNotFoundException.class, () -> controller.createSupply(request));
    verify(supplyRepository, never()).save(any(SupplyEntity.class));
  }

  // ---------------------------------------------------------------------------
  // U-I-04: shouldCreateSupplyVariant_whenSupplyAndUnitExist
  // ---------------------------------------------------------------------------

  @Test
  void shouldCreateSupplyVariant_whenSupplyAndUnitExist() {
    // Arrange
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();
    SupplyEntity supply = SupplyEntity.builder().id(3L).name("Pan").category(category).build();
    UnitOfMeasureEntity unit =
        UnitOfMeasureEntity.builder().id(2L).name("Gramos").abbreviation("g").build();
    BigDecimal quantity = new BigDecimal("0.500");

    SupplyVariantEntity savedVariant =
        SupplyVariantEntity.builder().id(7L).supply(supply).unit(unit).quantity(quantity).build();

    StorageLocationEntity bodega = StorageLocationEntity.builder().id(1L).name("Bodega").build();
    StorageLocationEntity cocina = StorageLocationEntity.builder().id(2L).name("Cocina").build();

    when(supplyRepository.findById(3L)).thenReturn(Optional.of(supply));
    when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(unit));
    when(supplyVariantRepository.findBySupplyIdAndUnitIdAndQuantity(3L, 2L, quantity))
        .thenReturn(Optional.empty());
    when(supplyVariantRepository.save(any(SupplyVariantEntity.class))).thenReturn(savedVariant);
    when(storageLocationRepository.findByName("Bodega")).thenReturn(Optional.of(bodega));
    when(storageLocationRepository.findByName("Cocina")).thenReturn(Optional.of(cocina));
    when(inventoryStockRepository.save(any(InventoryStockEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CreateSupplyVariantRequest request = new CreateSupplyVariantRequest(3L, 2L, quantity);

    // Act
    ResponseEntity<?> response = controller.createVariant(request);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(supplyVariantRepository, times(1)).save(any(SupplyVariantEntity.class));
    // Stock inicializado en Bodega y Cocina
    verify(inventoryStockRepository, times(2)).save(any(InventoryStockEntity.class));
  }

  // ---------------------------------------------------------------------------
  // U-I-05: shouldThrowException_whenVariantCombinationIsDuplicated
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowException_whenVariantCombinationIsDuplicated() {
    // Arrange
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();
    SupplyEntity supply = SupplyEntity.builder().id(3L).name("Pan").category(category).build();
    UnitOfMeasureEntity unit =
        UnitOfMeasureEntity.builder().id(2L).name("Gramos").abbreviation("g").build();
    BigDecimal quantity = new BigDecimal("0.500");

    SupplyVariantEntity existing =
        SupplyVariantEntity.builder().id(4L).supply(supply).unit(unit).quantity(quantity).build();

    when(supplyRepository.findById(3L)).thenReturn(Optional.of(supply));
    when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(unit));
    when(supplyVariantRepository.findBySupplyIdAndUnitIdAndQuantity(3L, 2L, quantity))
        .thenReturn(Optional.of(existing));

    CreateSupplyVariantRequest request = new CreateSupplyVariantRequest(3L, 2L, quantity);

    // Act & Assert
    assertThrows(
        SupplyVariantAlreadyExistsException.class, () -> controller.createVariant(request));
    verify(supplyVariantRepository, never()).save(any(SupplyVariantEntity.class));
  }

  // ---------------------------------------------------------------------------
  // U-I-06: shouldInitializeStockToZero_whenVariantIsCreated
  // ---------------------------------------------------------------------------

  @Test
  void shouldInitializeStockToZero_whenVariantIsCreated() {
    // Arrange
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();
    SupplyEntity supply = SupplyEntity.builder().id(3L).name("Pan").category(category).build();
    UnitOfMeasureEntity unit =
        UnitOfMeasureEntity.builder().id(2L).name("Gramos").abbreviation("g").build();
    BigDecimal quantity = new BigDecimal("0.500");

    SupplyVariantEntity savedVariant =
        SupplyVariantEntity.builder().id(7L).supply(supply).unit(unit).quantity(quantity).build();

    StorageLocationEntity bodega = StorageLocationEntity.builder().id(1L).name("Bodega").build();
    StorageLocationEntity cocina = StorageLocationEntity.builder().id(2L).name("Cocina").build();

    when(supplyRepository.findById(3L)).thenReturn(Optional.of(supply));
    when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(unit));
    when(supplyVariantRepository.findBySupplyIdAndUnitIdAndQuantity(3L, 2L, quantity))
        .thenReturn(Optional.empty());
    when(supplyVariantRepository.save(any(SupplyVariantEntity.class))).thenReturn(savedVariant);
    when(storageLocationRepository.findByName("Bodega")).thenReturn(Optional.of(bodega));
    when(storageLocationRepository.findByName("Cocina")).thenReturn(Optional.of(cocina));
    when(inventoryStockRepository.save(any(InventoryStockEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CreateSupplyVariantRequest request = new CreateSupplyVariantRequest(3L, 2L, quantity);

    // Act
    controller.createVariant(request);

    // Assert: capturar los dos argumentos de inventoryStockRepository.save()
    ArgumentCaptor<InventoryStockEntity> stockCaptor =
        ArgumentCaptor.forClass(InventoryStockEntity.class);
    verify(inventoryStockRepository, times(2)).save(stockCaptor.capture());

    List<InventoryStockEntity> savedStocks = stockCaptor.getAllValues();

    // Verificar que ambos registros tienen currentQuantity = 0
    savedStocks.forEach(stock -> assertEquals(BigDecimal.ZERO, stock.getCurrentQuantity()));

    // Verificar que uno corresponde a Bodega y otro a Cocina
    List<Long> locationIds =
        savedStocks.stream().map(s -> s.getStorageLocation().getId()).sorted().toList();
    assertEquals(List.of(1L, 2L), locationIds);
  }
}
