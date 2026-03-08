/* (C) 2026 */
package aros.services.rms.infraestructure.product.api;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.infraestructure.product.api.dto.ProductRequest;
import aros.services.rms.infraestructure.product.api.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

/** REST controller for product management. */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management")
public class ProductController {

  private final ProductUseCase productUseCase;

  @Operation(
      summary = "Create new product",
      description =
          "Creates a new product linked to an area and category. The hasOptions flag determines if customization options can be associated.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Area or category not found")
      })
  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    Product product =
        Product.builder()
            .name(request.name())
            .basePrice(request.basePrice())
            .hasOptions(request.hasOptions())
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .build();

    Product created = productUseCase.create(product);
    return new ResponseEntity<>(ProductResponse.fromDomain(created), HttpStatus.CREATED);
  }

  @Operation(
      summary = "Update product",
      description = "Updates an existing product's details.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Product, area, or category not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(
      @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
    Product product =
        Product.builder()
            .name(request.name())
            .basePrice(request.basePrice())
            .hasOptions(request.hasOptions())
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .build();

    Product updated = productUseCase.update(id, product);
    return ResponseEntity.ok(ProductResponse.fromDomain(updated));
  }

  @Operation(
      summary = "Get all products",
      description = "Retrieves all products.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<ProductResponse>> findAll() {
    List<ProductResponse> responses =
        productUseCase.findAll().stream()
            .map(ProductResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @Operation(
      summary = "Get product by ID",
      description = "Retrieves a product by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
    Product product = productUseCase.findById(id);
    return ResponseEntity.ok(ProductResponse.fromDomain(product));
  }

  @Operation(
      summary = "Disable product",
      description =
          "Performs logical deletion by setting the product as inactive. No physical deletion is performed.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product disabled successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  @PutMapping("/{id}/disable")
  public ResponseEntity<ProductResponse> disable(@PathVariable Long id) {
    Product product = productUseCase.disable(id);
    return ResponseEntity.ok(ProductResponse.fromDomain(product));
  }
}