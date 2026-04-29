/* (C) 2026 */
package aros.services.rms.infraestructure.category.api;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.input.CategoryUseCase;
import aros.services.rms.infraestructure.category.api.dto.CategoryRequest;
import aros.services.rms.infraestructure.category.api.dto.CategoryResponse;
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

/** REST controller for product category management. */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category management")
public class CategoryController {

  private final CategoryUseCase categoryUseCase;

  /**
   * Creates a new category.
   *
   * @param request the category request
   * @return the created category
   */
  @Operation(
      summary = "Create new category",
      description = "Creates a new product category.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  @PostMapping
  public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    Category category =
        Category.builder().name(request.name()).description(request.description()).build();

    Category created = categoryUseCase.create(category);
    return new ResponseEntity<>(CategoryResponse.fromDomain(created), HttpStatus.CREATED);
  }

  /**
   * Updates a category.
   *
   * @param id the category ID
   * @param request the category request
   * @return the updated category
   */
  @Operation(
      summary = "Update category",
      description = "Updates an existing product category's name and description.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Category not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponse> update(
      @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
    Category category =
        Category.builder().name(request.name()).description(request.description()).build();

    Category updated = categoryUseCase.update(id, category);
    return ResponseEntity.ok(CategoryResponse.fromDomain(updated));
  }

  /**
   * Gets all categories.
   *
   * @return the list of categories
   */
  @Operation(
      summary = "Get all categories",
      description = "Retrieves all product categories.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
      })
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> findAll() {
    List<CategoryResponse> responses =
        categoryUseCase.findAll().stream()
            .map(CategoryResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets a category by ID.
   *
   * @param id the category ID
   * @return the category
   */
  @Operation(
      summary = "Get category by ID",
      description = "Retrieves a product category by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
    Category category = categoryUseCase.findById(id);
    return ResponseEntity.ok(CategoryResponse.fromDomain(category));
  }

  /**
   * Toggles category enabled status.
   *
   * @param id the category ID
   * @return the updated category
   */
  @Operation(
      summary = "Toggle category enabled status",
      description = "Toggles the enabled/disabled status of a product category.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Category status toggled successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
      })
  @PutMapping("/{id}/toggle")
  public ResponseEntity<CategoryResponse> toggleEnabled(@PathVariable Long id) {
    Category category = categoryUseCase.toggleEnabled(id);
    return ResponseEntity.ok(CategoryResponse.fromDomain(category));
  }
}
