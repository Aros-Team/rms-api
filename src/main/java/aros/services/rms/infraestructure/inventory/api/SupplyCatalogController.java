/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api;

import aros.services.rms.core.inventory.application.exception.SupplyAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyCategoryAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantAlreadyExistsException;
import aros.services.rms.infraestructure.inventory.api.dto.CreateSupplyCategoryRequest;
import aros.services.rms.infraestructure.inventory.api.dto.CreateSupplyRequest;
import aros.services.rms.infraestructure.inventory.api.dto.CreateSupplyVariantRequest;
import aros.services.rms.infraestructure.inventory.api.dto.SupplyCategoryResponse;
import aros.services.rms.infraestructure.inventory.api.dto.SupplyResponse;
import aros.services.rms.infraestructure.inventory.api.dto.SupplyVariantResponse;
import aros.services.rms.infraestructure.inventory.api.dto.UnitOfMeasureResponse;
import aros.services.rms.infraestructure.inventory.persistence.InventoryStockEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyCategoryEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import aros.services.rms.infraestructure.inventory.persistence.jpa.InventoryStockRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.StorageLocationRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyCategoryRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.UnitOfMeasureRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Catalog controller for supplies, variants and categories. */
@RestController
@RequestMapping("/api/v1/supplies")
@RequiredArgsConstructor
@Tag(name = "Supply Catalog", description = "Catalog management for supplies, variants and categories")
public class SupplyCatalogController {

  private final SupplyRepository supplyRepository;
  private final SupplyVariantRepository supplyVariantRepository;
  private final SupplyCategoryRepository supplyCategoryRepository;
  private final InventoryStockRepository inventoryStockRepository;
  private final StorageLocationRepository storageLocationRepository;
  private final UnitOfMeasureRepository unitOfMeasureRepository;

  // ---------------------------------------------------------------------------
  // Categories
  // ---------------------------------------------------------------------------

  @Operation(
      summary = "List supply categories",
      description = "Returns all supply categories.",
      responses = {@ApiResponse(responseCode = "200", description = "OK")})
  @GetMapping("/categories")
  public ResponseEntity<List<SupplyCategoryResponse>> findAllCategories() {
    var categories =
        supplyCategoryRepository.findAll().stream()
            .map(SupplyCategoryResponse::fromEntity)
            .collect(Collectors.toList());
    return ResponseEntity.ok(categories);
  }

  @Operation(
      summary = "Create supply category",
      description = "Creates a new supply category. Returns 409 if the name already exists.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Category created"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
      })
  @PostMapping("/categories")
  @Transactional
  public ResponseEntity<SupplyCategoryResponse> createCategory(
      @Valid @RequestBody CreateSupplyCategoryRequest request) {
    // Check name uniqueness
    if (supplyCategoryRepository.findByNameIgnoreCase(request.name()).isPresent()) {
      throw new SupplyCategoryAlreadyExistsException(request.name());
    }
    var saved =
        supplyCategoryRepository.save(
            SupplyCategoryEntity.builder().name(request.name()).build());
    return new ResponseEntity<>(SupplyCategoryResponse.fromEntity(saved), HttpStatus.CREATED);
  }

  // ---------------------------------------------------------------------------
  // Units of measure
  // ---------------------------------------------------------------------------

  @Operation(
      summary = "List units of measure",
      description = "Returns all units of measure. Use to populate the unit selector when creating a variant.",
      responses = {@ApiResponse(responseCode = "200", description = "OK")})
  @GetMapping("/units")
  public ResponseEntity<List<UnitOfMeasureResponse>> findAllUnits() {
    var units =
        unitOfMeasureRepository.findAll().stream()
            .map(UnitOfMeasureResponse::fromEntity)
            .collect(Collectors.toList());
    return ResponseEntity.ok(units);
  }

  // ---------------------------------------------------------------------------
  // Supplies (insumos base)
  // ---------------------------------------------------------------------------

  @Operation(
      summary = "List supplies",
      description = "Returns all supplies. Filter by categoryId and/or name (partial, case-insensitive).",
      responses = {@ApiResponse(responseCode = "200", description = "OK")})
  @GetMapping
  @Transactional(readOnly = true)
  public ResponseEntity<List<SupplyResponse>> findAllSupplies(
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String name) {

    var supplies =
        (categoryId != null && name != null)
            ? supplyRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, name)
            : (categoryId != null)
                ? supplyRepository.findByCategoryId(categoryId)
                : (name != null)
                    ? supplyRepository.findByNameContainingIgnoreCase(name)
                    : supplyRepository.findAll();

    var responses = supplies.stream().map(SupplyResponse::fromEntity).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @Operation(
      summary = "Create supply",
      description =
          "Creates a new supply (insumo base). Only requires a name and a categoryId."
              + " Returns 409 if the name already exists.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Supply created"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Supply name already exists")
      })
  @PostMapping
  @Transactional
  public ResponseEntity<SupplyResponse> createSupply(
      @Valid @RequestBody CreateSupplyRequest request) {
    // Validate category exists
    var category =
        supplyCategoryRepository
            .findById(request.categoryId())
            .orElseThrow(
                () ->
                    new aros.services.rms.core.inventory.application.exception
                        .StorageLocationNotFoundException(
                        "Category not found: id=" + request.categoryId()));

    // Check name uniqueness
    if (supplyRepository.findByNameIgnoreCase(request.name()).isPresent()) {
      throw new SupplyAlreadyExistsException(request.name());
    }

    var saved =
        supplyRepository.save(
            SupplyEntity.builder().name(request.name()).category(category).build());
    return new ResponseEntity<>(SupplyResponse.fromEntity(saved), HttpStatus.CREATED);
  }

  // ---------------------------------------------------------------------------
  // Supply variants
  // ---------------------------------------------------------------------------

  @Operation(
      summary = "List supply variants with stock",
      description =
          "Returns all supply variants with current stock in Bodega and Cocina."
              + " Optionally filter by supplyId."
              + " The variant 'id' is the supplyVariantId used in purchase order items.",
      responses = {@ApiResponse(responseCode = "200", description = "OK")})
  @GetMapping("/variants")
  @Transactional(readOnly = true)
  public ResponseEntity<List<SupplyVariantResponse>> findAllVariants(
      @RequestParam(required = false) Long supplyId) {

    var variants =
        supplyId != null
            ? supplyVariantRepository.findBySupplyId(supplyId)
            : supplyVariantRepository.findAll();

    Long bodegaId = resolveLocationId("Bodega");
    Long cocinaId = resolveLocationId("Cocina");
    Map<Long, BigDecimal> bodegaStock = buildStockMap(bodegaId);
    Map<Long, BigDecimal> cocinaStock = buildStockMap(cocinaId);

    var responses =
        variants.stream()
            .map(
                v ->
                    SupplyVariantResponse.fromEntity(
                        v,
                        bodegaStock.getOrDefault(v.getId(), BigDecimal.ZERO),
                        cocinaStock.getOrDefault(v.getId(), BigDecimal.ZERO)))
            .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }

  @Operation(
      summary = "Create supply variant",
      description =
          "Creates a new supply variant (physical presentation of a supply) and initializes"
              + " stock to zero in Bodega and Cocina."
              + " The combination supplyId + unitId + quantity must be unique."
              + " Returns 409 if it already exists.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Variant created with zero stock"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Supply or unit of measure not found"),
        @ApiResponse(responseCode = "409", description = "Variant already exists")
      })
  @PostMapping("/variants")
  @Transactional
  public ResponseEntity<SupplyVariantResponse> createVariant(
      @Valid @RequestBody CreateSupplyVariantRequest request) {
    // Validate supply exists
    var supply =
        supplyRepository
            .findById(request.supplyId())
            .orElseThrow(() -> new SupplyNotFoundException(request.supplyId()));

    // Validate unit of measure exists
    var unit =
        unitOfMeasureRepository
            .findById(request.unitId())
            .orElseThrow(
                () ->
                    new aros.services.rms.core.inventory.application.exception
                        .StorageLocationNotFoundException(
                        "Unit of measure not found: id=" + request.unitId()));

    // Check uniqueness: same supply + unit + quantity
    if (supplyVariantRepository
        .findBySupplyIdAndUnitIdAndQuantity(request.supplyId(), request.unitId(), request.quantity())
        .isPresent()) {
      throw new SupplyVariantAlreadyExistsException(
          request.supplyId(), request.unitId(), request.quantity().toPlainString());
    }

    // Persist variant
    var saved =
        supplyVariantRepository.save(
            SupplyVariantEntity.builder().supply(supply).unit(unit).quantity(request.quantity()).build());

    // Initialize stock to zero in Bodega and Cocina
    initStockIfLocationExists(saved, "Bodega");
    initStockIfLocationExists(saved, "Cocina");

    return new ResponseEntity<>(
        SupplyVariantResponse.fromEntity(saved, BigDecimal.ZERO, BigDecimal.ZERO),
        HttpStatus.CREATED);
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  private void initStockIfLocationExists(SupplyVariantEntity variant, String locationName) {
    storageLocationRepository
        .findByName(locationName)
        .ifPresent(
            location ->
                inventoryStockRepository.save(
                    InventoryStockEntity.builder()
                        .supplyVariant(variant)
                        .storageLocation(location)
                        .currentQuantity(BigDecimal.ZERO)
                        .build()));
  }

  private Long resolveLocationId(String name) {
    return storageLocationRepository.findByName(name).map(loc -> loc.getId()).orElse(null);
  }

  private Map<Long, BigDecimal> buildStockMap(Long locationId) {
    if (locationId == null) return Map.of();
    return inventoryStockRepository.findByStorageLocationId(locationId).stream()
        .collect(
            Collectors.toMap(
                stock -> stock.getSupplyVariant().getId(),
                InventoryStockEntity::getCurrentQuantity));
  }
}
