/* (C) 2026 */

package aros.services.rms.infraestructure.category.api;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.input.CategoryUseCase;
import aros.services.rms.infraestructure.category.api.dto.CategoryRequest;
import aros.services.rms.infraestructure.category.api.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for product category management. */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(
    name = "Categories",
    description = "Operations for managing menu product categories and their enabled status")
public class CategoryController {

  private final CategoryUseCase categoryUseCase;

  /**
   * Creates a new category.
   *
   * @param request the category request
   * @return the created category
   */
  @Operation(
      tags = {"Categories"},
      summary = "Create new category",
      description = "Creates a new product category for the menu.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Category name already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
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
      tags = {"Categories"},
      summary = "Update category",
      description = "Updates the name and description of an existing product category.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponse> update(
      @Parameter(description = "Category ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody CategoryRequest request) {
    Category category =
        Category.builder().name(request.name()).description(request.description()).build();

    Category updated = categoryUseCase.update(id, category);
    return ResponseEntity.ok(CategoryResponse.fromDomain(updated));
  }

  /**
   * Gets all categories, optionally filtered by name.
   *
   * @param search optional name filter (partial, case-insensitive)
   * @return the list of categories
   */
  @Operation(
      tags = {"Categories"},
      summary = "Get all categories",
      description =
          "Returns all product categories from the menu. "
              + "Optionally filters by name (partial, case-insensitive).",
      responses = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> findAll(
      @Parameter(
              description = "Optional name filter (partial, case-insensitive)",
              example = "tamaño")
          @RequestParam(required = false)
          String search) {
    List<Category> categories;
    if (search != null && !search.isBlank()) {
      categories = categoryUseCase.findByNameContainingIgnoreCase(search);
    } else {
      categories = categoryUseCase.findAll();
    }
    List<CategoryResponse> responses =
        categories.stream().map(CategoryResponse::fromDomain).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets a category by ID.
   *
   * @param id the category ID
   * @return the category
   */
  @Operation(
      tags = {"Categories"},
      summary = "Get category by ID",
      description = "Returns a specific product category given its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> findById(
      @Parameter(description = "Category ID", example = "1", required = true) @PathVariable
          Long id) {
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
      tags = {"Categories"},
      summary = "Toggle category active status",
      description = "Changes the enabled/disabled status of a product category.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Category status changed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/toggle")
  public ResponseEntity<CategoryResponse> toggleEnabled(
      @Parameter(description = "Category ID", example = "1", required = true) @PathVariable
          Long id) {
    Category category = categoryUseCase.toggleEnabled(id);
    return ResponseEntity.ok(CategoryResponse.fromDomain(category));
  }
}
