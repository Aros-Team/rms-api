/* (C) 2026 */

package aros.services.rms.infraestructure.product.api;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.input.ProductOptionUseCase;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.infraestructure.product.api.dto.ProductOptionResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductRequest;
import aros.services.rms.infraestructure.product.api.dto.ProductResponse;
import aros.services.rms.infraestructure.product.api.dto.RecipeItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for product management. */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Gestión del menú de productos del restaurante")
public class ProductController {

  private final ProductUseCase productUseCase;
  private final ProductOptionUseCase productOptionUseCase;

  /**
   * Creates a new product.
   *
   * @param request the product request
   * @return the created product
   */
  @Operation(
      summary = "Crear nuevo producto",
      description =
          "Crea un nuevo producto vinculado a un área de preparación y categoría. "
              + "Se pueden asociar opciones de personalización mediante optionIds.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Área o categoría no encontrada")
      })
  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .basePrice(request.basePrice())
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .optionIds(request.optionIds())
            .recipe(recipe)
            .build();

    Product created = productUseCase.create(product);
    return new ResponseEntity<>(ProductResponse.fromDomain(created), HttpStatus.CREATED);
  }

  /**
   * Updates a product.
   *
   * @param id the product ID
   * @param request the update request
   * @return the updated product
   */
  @Operation(
      summary = "Actualizar producto",
      description =
          "Actualiza los detalles de un producto existente (nombre, precio, categoría, área).",
      responses = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto, área o categoría no encontrada")
      })
  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(
      @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .basePrice(request.basePrice())
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .optionIds(request.optionIds())
            .recipe(recipe)
            .build();

    Product updated = productUseCase.update(id, product);
    return ResponseEntity.ok(ProductResponse.fromDomain(updated));
  }

  /**
   * Gets all products.
   *
   * @param categories optional category filter
   * @return the list of products
   */
  @Operation(
      summary = "Obtener todos los productos",
      description =
          "Retorna lista de todos los productos activos e inactivos del menú. "
              + "Puede filtrarse por categoría usando el parámetro 'categories'.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente")
      })
  @GetMapping
  public ResponseEntity<List<ProductResponse>> findAll(
      @RequestParam(required = false) List<Long> categories) {
    List<ProductResponse> responses;
    if (categories == null || categories.isEmpty()) {
      responses =
          productUseCase.findAll().stream()
              .map(ProductResponse::fromDomain)
              .collect(Collectors.toList());
    } else {
      responses =
          productUseCase.findByCategoryIds(categories).stream()
              .map(ProductResponse::fromDomain)
              .collect(Collectors.toList());
    }
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets top selling products.
   *
   * @return the list of top selling products
   */
  @Operation(
      summary = "Obtener productos más vendidos",
      description = "Retorna los productos más vendidos del restaurante.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente")
      })
  @GetMapping("/top-selling")
  public ResponseEntity<List<ProductResponse>> getTopSelling() {
    List<ProductResponse> responses =
        List.of(); // TODO: Implementar lógica de productos más vendidos
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets a product by ID.
   *
   * @param id the product ID
   * @return the product
   */
  @Operation(
      summary = "Obtener producto por ID",
      description = "Retorna un producto específico dado su identificador.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Producto obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
      })
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
    Product product = productUseCase.findById(id);
    return ResponseEntity.ok(ProductResponse.fromDomain(product));
  }

  /**
   * Disables a product.
   *
   * @param id the product ID
   * @return the disabled product
   */
  @Operation(
      summary = "Desactivar producto",
      description =
          "Realiza eliminación lógica estableciendo el producto como inactivo. "
              + "No se realiza eliminación física.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Producto desactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
      })
  @PutMapping("/{id}/disable")
  public ResponseEntity<ProductResponse> disable(@PathVariable Long id) {
    Product product = productUseCase.disable(id);
    return ResponseEntity.ok(ProductResponse.fromDomain(product));
  }

  /**
   * Gets options for a product.
   *
   * @param id the product ID
   * @return the list of product options
   */
  @Operation(
      summary = "Obtener opciones de un producto",
      description = "Retorna las opciones de personalización asociadas a un producto específico.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Opciones obtenidas exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
      })
  @GetMapping("/{id}/options")
  public ResponseEntity<List<ProductOptionResponse>> findOptionsByProductId(@PathVariable Long id) {
    // First verify the product exists
    productUseCase.findById(id);

    List<ProductOptionResponse> responses =
        productOptionUseCase.findByProductId(id).stream()
            .map(ProductOptionResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  private List<ProductRecipe> mapRecipe(List<RecipeItemRequest> recipeRequests) {
    if (recipeRequests == null || recipeRequests.isEmpty()) {
      return new ArrayList<>();
    }
    return recipeRequests.stream()
        .map(
            item ->
                ProductRecipe.builder()
                    .supplyVariantId(item.supplyVariantId())
                    .requiredQuantity(item.requiredQuantity())
                    .build())
        .collect(Collectors.toList());
  }
}
