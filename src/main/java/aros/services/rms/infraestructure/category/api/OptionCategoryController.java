/* (C) 2026 */

package aros.services.rms.infraestructure.category.api;

import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.port.input.OptionCategoryUseCase;
import aros.services.rms.infraestructure.category.api.dto.CategoryRequest;
import aros.services.rms.infraestructure.category.api.dto.OptionCategoryResponse;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for option category management. Option categories define customization types
 * (e.g., "Cooking term", "Milk type"), different from product categories.
 */
@RestController
@RequestMapping("/api/v1/option-categories")
@RequiredArgsConstructor
@Tag(
    name = "Option Categories",
    description =
        "Operations for managing option categories used in product customization"
            + " (e.g. cooking term, milk type)")
public class OptionCategoryController {

  private final OptionCategoryUseCase optionCategoryUseCase;

  /**
   * Creates a new option category.
   *
   * @param request the category request
   * @return the created option category
   */
  @Operation(
      tags = {"Option Categories"},
      summary = "Create new option category",
      description = "Creates a new option category for product customization.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Option category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Option category name already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<OptionCategoryResponse> create(
      @Valid @RequestBody CategoryRequest request) {
    OptionCategory optionCategory =
        OptionCategory.builder().name(request.name()).description(request.description()).build();

    OptionCategory created = optionCategoryUseCase.create(optionCategory);
    return new ResponseEntity<>(OptionCategoryResponse.fromDomain(created), HttpStatus.CREATED);
  }

  /**
   * Updates an option category.
   *
   * @param id the option category ID
   * @param request the category request
   * @return the updated option category
   */
  @Operation(
      tags = {"Option Categories"},
      summary = "Update option category",
      description = "Updates an existing option category.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Option category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Option category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<OptionCategoryResponse> update(
      @Parameter(description = "Option category ID", example = "1", required = true) @PathVariable
          Long id,
      @Valid @RequestBody CategoryRequest request) {
    OptionCategory optionCategory =
        OptionCategory.builder().name(request.name()).description(request.description()).build();

    OptionCategory updated = optionCategoryUseCase.update(id, optionCategory);
    return ResponseEntity.ok(OptionCategoryResponse.fromDomain(updated));
  }

  /**
   * Gets all option categories.
   *
   * @return the list of option categories
   */
  @Operation(
      tags = {"Option Categories"},
      summary = "Get all option categories",
      description = "Returns all option categories for customization.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Option categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<OptionCategoryResponse>> findAll() {
    List<OptionCategoryResponse> responses =
        optionCategoryUseCase.findAll().stream()
            .map(OptionCategoryResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets an option category by ID.
   *
   * @param id the option category ID
   * @return the option category
   */
  @Operation(
      tags = {"Option Categories"},
      summary = "Get option category by ID",
      description = "Returns a specific option category given its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Option category retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Option category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<OptionCategoryResponse> findById(
      @Parameter(description = "Option category ID", example = "1", required = true) @PathVariable
          Long id) {
    OptionCategory optionCategory = optionCategoryUseCase.findById(id);
    return ResponseEntity.ok(OptionCategoryResponse.fromDomain(optionCategory));
  }
}
