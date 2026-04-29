/* (C) 2026 */
package aros.services.rms.infraestructure.product.api;

import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.input.ProductOptionUseCase;
import aros.services.rms.infraestructure.product.api.dto.ProductOptionRequest;
import aros.services.rms.infraestructure.product.api.dto.ProductOptionResponse;
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
import org.springframework.web.bind.annotation.RestController;

/** REST controller for product option management. */
@RestController
@RequestMapping("/api/v1/product-options")
@RequiredArgsConstructor
@Tag(name = "Product Options", description = "Product option management for customization choices")
public class ProductOptionController {

  private final ProductOptionUseCase productOptionUseCase;

  /**
   * Creates a new product option.
   *
   * @param request the product option request
   * @return the created product option
   */
  @Operation(
      summary = "Create new product option",
      description = "Creates a new product option linked to an option category.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Product option created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Option category not found")
      })
  @PostMapping
  public ResponseEntity<ProductOptionResponse> create(
      @Valid @RequestBody ProductOptionRequest request) {
    List<OptionRecipe> recipe = mapRecipe(request.recipe());
    ProductOption option =
        ProductOption.builder()
            .name(request.name())
            .category(OptionCategory.builder().id(request.optionCategoryId()).build())
            .recipe(recipe)
            .build();

    ProductOption created = productOptionUseCase.create(option);
    return new ResponseEntity<>(ProductOptionResponse.fromDomain(created), HttpStatus.CREATED);
  }

  /**
   * Updates a product option.
   *
   * @param id the product option ID
   * @param request the update request
   * @return the updated product option
   */
  @Operation(
      summary = "Update product option",
      description = "Updates an existing product option.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product option updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product option or category not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<ProductOptionResponse> update(
      @PathVariable Long id, @Valid @RequestBody ProductOptionRequest request) {
    List<OptionRecipe> recipe = mapRecipe(request.recipe());
    ProductOption option =
        ProductOption.builder()
            .name(request.name())
            .category(OptionCategory.builder().id(request.optionCategoryId()).build())
            .recipe(recipe)
            .build();

    ProductOption updated = productOptionUseCase.update(id, option);
    return ResponseEntity.ok(ProductOptionResponse.fromDomain(updated));
  }

  /**
   * Gets all product options.
   *
   * @return the list of product options
   */
  @Operation(
      summary = "Get all product options",
      description = "Retrieves all product options.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product options retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<ProductOptionResponse>> findAll() {
    List<ProductOptionResponse> responses =
        productOptionUseCase.findAll().stream()
            .map(ProductOptionResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets a product option by ID.
   *
   * @param id the product option ID
   * @return the product option
   */
  @Operation(
      summary = "Get product option by ID",
      description = "Retrieves a product option by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product option retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product option not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<ProductOptionResponse> findById(@PathVariable Long id) {
    ProductOption option = productOptionUseCase.findById(id);
    return ResponseEntity.ok(ProductOptionResponse.fromDomain(option));
  }

  private List<OptionRecipe> mapRecipe(List<RecipeItemRequest> recipeRequests) {
    if (recipeRequests == null || recipeRequests.isEmpty()) {
      return new ArrayList<>();
    }
    return recipeRequests.stream()
        .map(
            item ->
                OptionRecipe.builder()
                    .supplyVariantId(item.supplyVariantId())
                    .requiredQuantity(item.requiredQuantity())
                    .build())
        .collect(Collectors.toList());
  }
}
