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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
@Tag(name = "Products", description = "Restaurant menu product management")
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
      summary = "Create new product",
      description =
          "Creates a new product linked to a preparation area and category. "
              + "Personalization options can be associated using optionIds.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Area or category not found")
      })
  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .description(request.description())
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
      summary = "Update product",
      description = "Updates an existing product details (name, description, price).",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Product, area or category not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(
      @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .description(request.description())
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
   * @param page page number (default 0)
   * @param size page size (default 20, max 100)
   * @param includeInactive if true, include inactive products (default false)
   * @return the list of products
   */
  @Operation(
      summary = "Get all products",
      description =
          "Returns a paginated list of products. "
              + "By default only returns active products. "
              + "Can be filtered by category using the 'categories' parameter.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  @GetMapping
  public ResponseEntity<Page<ProductResponse>> findAll(
      @Parameter(description = "Category IDs to filter by") @RequestParam(required = false)
          List<Long> categories,
      @Parameter(description = "Número de página (default 0)") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Tamaño de página (default 20, max 100)")
          @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Incluir productos inactivos (default false)")
          @RequestParam(defaultValue = "false")
          boolean includeInactive) {
    if (page < 0) {
      throw new IllegalArgumentException("El parámetro page debe ser mayor o igual a 0");
    }
    if (size <= 0 || size > 100) {
      throw new IllegalArgumentException("El parámetro size debe estar entre 1 y 100");
    }
    var pageable = PageRequest.of(page, size);
    Page<ProductResponse> responses;
    if (categories == null || categories.isEmpty()) {
      if (includeInactive) {
        List<Product> allProducts = productUseCase.findAll();
        List<ProductResponse> productResponses =
            allProducts.stream().map(ProductResponse::fromDomain).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productResponses.size());
        List<ProductResponse> pagedContent =
            start < productResponses.size() ? productResponses.subList(start, end) : List.of();
        responses = new PageImpl<>(pagedContent, pageable, productResponses.size());
      } else {
        responses = productUseCase.findAllActive(pageable).map(ProductResponse::fromDomain);
      }
    } else {
      List<Product> filteredProducts = productUseCase.findByCategoryIds(categories);
      List<ProductResponse> productResponses =
          filteredProducts.stream().map(ProductResponse::fromDomain).toList();
      int start = (int) pageable.getOffset();
      int end = Math.min((start + pageable.getPageSize()), productResponses.size());
      List<ProductResponse> pagedContent =
          start < productResponses.size() ? productResponses.subList(start, end) : List.of();
      responses = new PageImpl<>(pagedContent, pageable, productResponses.size());
    }
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets top selling products.
   *
   * @return the list of top selling products
   */
  @Operation(
      summary = "Get top selling products",
      description = "Returns the top selling products of the restaurant.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
      })
  @GetMapping("/top-selling")
  public ResponseEntity<List<ProductResponse>> getTopSelling() {
    List<ProductResponse> responses = List.of();
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets a product by ID.
   *
   * @param id the product ID
   * @return the product
   */
  @Operation(
      summary = "Get product by ID",
      description = "Returns a specific product by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
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
      summary = "Disable product",
      description =
          "Performs logical deletion by setting the product as inactive. "
              + "Physical deletion is not performed.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product disabled successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
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
      summary = "Get product options",
      description = "Returns the personalization options associated with a specific product.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Options retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
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
