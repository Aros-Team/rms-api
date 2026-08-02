/* (C) 2026 */

package aros.services.rms.infraestructure.category.api;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import aros.services.rms.core.product.port.input.ProductOptionUseCase;
import aros.services.rms.infraestructure.category.api.dto.OptionGroupRequest;
import aros.services.rms.infraestructure.category.api.dto.OptionGroupResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductOptionResponse;
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
 * REST controller for option group management. Option groups define customization buckets for
 * products (e.g., "Proteína Hamburguesa", "Acompañamiento Parrilla"), different from product
 * categories.
 */
@RestController
@RequestMapping("/api/v1/option-groups")
@RequiredArgsConstructor
@Tag(
    name = "Option Groups",
    description =
        "Operations for managing option groups used in product customization"
            + " (e.g. protein type, side dish)")
public class OptionGroupController {

  private final OptionGroupUseCase optionGroupUseCase;
  private final ProductOptionUseCase productOptionUseCase;

  /**
   * Creates a new option group.
   *
   * @param request the option group request with product associations
   * @return the created option group
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Create new option group",
      description =
          "Creates a new option group and associates it with one or more products. "
              + "At least one product ID is required.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Option group created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or no products supplied"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<OptionGroupResponse> create(
      @Valid @RequestBody OptionGroupRequest request) {
    OptionGroup optionGroup =
        OptionGroup.builder()
            .name(request.name())
            .description(request.description())
            .selectionType(parseSelectionType(request.selectionType()))
            .build();

    OptionGroup created =
        optionGroupUseCase.create(optionGroup, request.productIds(), request.required());
    return new ResponseEntity<>(enrichAndMap(List.of(created)).getFirst(), HttpStatus.CREATED);
  }

  /**
   * Updates an option group.
   *
   * @param id the option group ID
   * @param request the option group request with product associations
   * @return the updated option group
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Update option group",
      description = "Updates an existing option group and replaces its product associations.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Option group updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or no products supplied"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Option group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<OptionGroupResponse> update(
      @Parameter(description = "Option group ID", example = "1", required = true) @PathVariable
          Long id,
      @Valid @RequestBody OptionGroupRequest request) {
    OptionGroup optionGroup =
        OptionGroup.builder()
            .name(request.name())
            .description(request.description())
            .selectionType(parseSelectionType(request.selectionType()))
            .build();

    OptionGroup updated =
        optionGroupUseCase.update(id, optionGroup, request.productIds(), request.required());
    return ResponseEntity.ok(enrichAndMap(List.of(updated)).getFirst());
  }

  /**
   * Gets all option groups, optionally filtered by name or product ID.
   *
   * @param search optional name filter (partial, case-insensitive)
   * @param productId optional product ID filter (only groups attached to this product)
   * @return the list of option groups
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Get all option groups",
      description =
          "Returns all option groups for customization. "
              + "Optionally filters by name (partial, case-insensitive) or product ID.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Option groups retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<OptionGroupResponse>> findAll(
      @Parameter(
              description = "Optional name filter (partial, case-insensitive)",
              example = "proteína")
          @RequestParam(required = false)
          String search,
      @Parameter(description = "Optional product ID filter", example = "1")
          @RequestParam(required = false)
          Long productId) {
    List<OptionGroup> groups;
    if (productId != null) {
      groups = optionGroupUseCase.findByProductId(productId);
      if (search != null && !search.isBlank()) {
        String lowerSearch = search.toLowerCase();
        groups =
            groups.stream()
                .filter(g -> g.getName() != null && g.getName().toLowerCase().contains(lowerSearch))
                .toList();
      }
    } else if (search != null && !search.isBlank()) {
      groups = optionGroupUseCase.findByNameContainingIgnoreCase(search);
    } else {
      groups = optionGroupUseCase.findAll();
    }
    List<OptionGroupResponse> responses = enrichAndMap(groups);
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
    List<OptionGroupResponse> responses = enrichAndMap(List.of(optionGroup));
    return ResponseEntity.ok(responses.getFirst());
  }

  /**
   * Gets all product options belonging to an option group.
   *
   * @param id the option group ID
   * @return the list of product options in the group (with category-level selection type and
   *     optionGroupId/Name populated; per-product cost/extraPrice default to zero)
   */
  @Operation(
      tags = {"Option Groups"},
      summary = "Get options for an option group",
      description =
          "Returns the product options attached to a specific option group. "
              + "Each option carries the group's selectionType "
              + "via the categorySelectionType field. "
              + "Per-product cost/extraPrice are not in scope for this endpoint; "
              + "use GET /products/{id}/options or /products/{id}/cost-breakdown for those.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Options retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Option group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}/options")
  public ResponseEntity<List<ProductOptionResponse>> findOptionsByGroupId(
      @Parameter(description = "Option group ID", example = "1", required = true) @PathVariable
          Long id) {
    optionGroupUseCase.findById(id);
    List<ProductOptionResponse> responses =
        productOptionUseCase.findByOptionGroupId(id).stream()
            .map(ProductOptionResponse::fromDomainInGroupContext)
            .toList();
    return ResponseEntity.ok(responses);
  }

  /**
   * Parses the {@code selectionType} string from the request into the domain enum. {@code null} or
   * blank values default to {@link OptionSelectionType#SINGLE_CHOICE}. Unknown values trigger a 400
   * via Spring's binding handler.
   *
   * @param value the raw string from the request (nullable)
   * @return the parsed selection type
   * @throws IllegalArgumentException if the value is non-blank and not a valid enum constant
   */
  private static OptionSelectionType parseSelectionType(String value) {
    if (value == null || value.isBlank()) {
      return OptionSelectionType.SINGLE_CHOICE;
    }
    try {
      return OptionSelectionType.valueOf(value.trim());
    } catch (IllegalArgumentException unknown) {
      throw new IllegalArgumentException(
          "Invalid selectionType: '"
              + value
              + "'. Allowed values: SINGLE_CHOICE, MULTI_CHOICE, ADD_ON, REMOVAL");
    }
  }

  private List<OptionGroupResponse> enrichAndMap(List<OptionGroup> groups) {
    if (groups.isEmpty()) {
      return List.of();
    }
    List<Long> groupIds = groups.stream().map(OptionGroup::getId).toList();
    Map<Long, String> selectionTypes = optionGroupUseCase.loadSelectionTypesByIds(groupIds);
    Map<Long, List<Long>> productIdsByGroup =
        optionGroupUseCase.loadProductIdsByOptionGroupIds(groupIds);
    return groups.stream()
        .map(
            g ->
                OptionGroupResponse.fromDomain(
                    g,
                    selectionTypes.get(g.getId()),
                    productIdsByGroup.getOrDefault(g.getId(), List.of())))
        .toList();
  }
}
