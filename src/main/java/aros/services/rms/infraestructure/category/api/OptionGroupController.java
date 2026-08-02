/* (C) 2026 */

package aros.services.rms.infraestructure.category.api;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import aros.services.rms.infraestructure.category.api.dto.CategoryRequest;
import aros.services.rms.infraestructure.category.api.dto.OptionGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

/**
 * REST controller for option group management. Option categories define customization types (e.g.,
 * "Cooking term", "Milk type"), different from product categories.
 */
@RestController
@RequestMapping("/api/v1/option-categories")
@RequiredArgsConstructor
@Tag(
    name = "Option Groups",
    description =
        "Operations for managing option categories used in product customization"
            + " (e.g. cooking term, milk type)")
public class OptionGroupController {

  private final OptionGroupUseCase optionGroupUseCase;

  /**
   * Creates a new option group.
   *
   * @param request the category request
   * @return the created option group
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Create new option group",
      description = "Creates a new option group for product customization.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Option group created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Option group name already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<OptionGroupResponse> create(@Valid @RequestBody CategoryRequest request) {
    OptionGroup optionGroup =
        OptionGroup.builder().name(request.name()).description(request.description()).build();

    OptionGroup created = optionGroupUseCase.create(optionGroup);
    return new ResponseEntity<>(toResponse(created), HttpStatus.CREATED);
  }

  /**
   * Updates an option group.
   *
   * @param id the option group ID
   * @param request the category request
   * @return the updated option group
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Update option group",
      description = "Updates an existing option group.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Option group updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Option group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<OptionGroupResponse> update(
      @Parameter(description = "Option group ID", example = "1", required = true) @PathVariable
          Long id,
      @Valid @RequestBody CategoryRequest request) {
    OptionGroup optionGroup =
        OptionGroup.builder().name(request.name()).description(request.description()).build();

    OptionGroup updated = optionGroupUseCase.update(id, optionGroup);
    return ResponseEntity.ok(toResponse(updated));
  }

  /**
   * Gets all option categories, optionally filtered by name.
   *
   * @param search optional name filter (partial, case-insensitive)
   * @return the list of option categories
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Get all option categories",
      description =
          "Returns all option categories for customization. "
              + "Optionally filters by name (partial, case-insensitive).",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Option categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<OptionGroupResponse>> findAll(
      @Parameter(
              description = "Optional name filter (partial, case-insensitive)",
              example = "tamaño")
          @RequestParam(required = false)
          String search) {
    List<OptionGroup> categories;
    if (search != null && !search.isBlank()) {
      categories = optionGroupUseCase.findByNameContainingIgnoreCase(search);
    } else {
      categories = optionGroupUseCase.findAll();
    }
    Map<Long, String> selectionTypes =
        optionGroupUseCase.loadSelectionTypesByIds(
            categories.stream().map(OptionGroup::getId).toList());
    List<OptionGroupResponse> responses =
        categories.stream()
            .map(
                category ->
                    OptionGroupResponse.fromDomain(category, selectionTypes.get(category.getId())))
            .toList();
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets an option group by ID.
   *
   * @param id the option group ID
   * @return the option group
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Get option group by ID",
      description = "Returns a specific option group given its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Option group retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Option group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<OptionGroupResponse> findById(
      @Parameter(description = "Option group ID", example = "1", required = true) @PathVariable
          Long id) {
    OptionGroup optionGroup = optionGroupUseCase.findById(id);
    return ResponseEntity.ok(toResponse(optionGroup));
  }

  private OptionGroupResponse toResponse(OptionGroup category) {
    String selectionType =
        optionGroupUseCase.loadSelectionTypesByIds(List.of(category.getId())).get(category.getId());
    return OptionGroupResponse.fromDomain(category, selectionType);
  }
}
