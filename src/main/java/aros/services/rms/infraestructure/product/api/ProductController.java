/* (C) 2026 */

package aros.services.rms.infraestructure.product.api;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductCost;
import aros.services.rms.core.product.domain.ProductCostBreakdown;
import aros.services.rms.core.product.port.input.CalculateProductCostUseCase;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.infraestructure.category.api.dto.OptionGroupResponse;
import aros.services.rms.infraestructure.product.api.dto.OptionExtrasRequest;
import aros.services.rms.infraestructure.product.api.dto.ProductCostResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductOptionResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductRequest;
import aros.services.rms.infraestructure.product.api.dto.ProductResponse;
import aros.services.rms.infraestructure.product.api.dto.RecipeItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
@Tag(
    name = "Products",
    description =
        "Operations for managing restaurant menu products, their recipes, options and lifecycle")
public class ProductController {

  private final ProductUseCase productUseCase;
  private final CalculateProductCostUseCase calculateProductCostUseCase;
  private final GetProductCostBreakdownUseCase getProductCostBreakdownUseCase;
  private final ImageRepositoryPort imageRepositoryPort;
  private final StoragePort storagePort;
  private final OptionGroupUseCase optionGroupUseCase;

  private static final Duration SIGNED_URL_EXPIRATION = Duration.ofMinutes(60);

  /**
   * Creates a new product.
   *
   * @param request the product request
   * @return the created product
   */
  @Operation(
      tags = {"Products"},
      summary = "Create new product",
      description =
          "Creates a new product linked to a preparation area and category. "
              + "Personalization options can be associated using optionIds.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Area or category not found"),
        @ApiResponse(responseCode = "409", description = "Product name already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .description(request.description())
            .basePrice(
                new Money(BigDecimal.valueOf(request.basePrice()), Currency.getInstance("COP")))
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .optionIds(request.optionIds())
            .optionExtras(mapOptionExtras(request.optionExtras()))
            .recipe(recipe)
            .estimatedPrepMinutes(request.estimatedPrepMinutes())
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
      tags = {"Products"},
      summary = "Update product",
      description = "Updates an existing product details (name, description, price, recipe).",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product, area or category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .description(request.description())
            .basePrice(
                new Money(BigDecimal.valueOf(request.basePrice()), Currency.getInstance("COP")))
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .optionIds(request.optionIds())
            .optionExtras(mapOptionExtras(request.optionExtras()))
            .recipe(recipe)
            .estimatedPrepMinutes(request.estimatedPrepMinutes())
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
   * @param includeSelections if true, include special selection products (default false)
   * @param search optional name/description/category filter (partial, case-insensitive)
   * @return the list of products
   */
  @Operation(
      tags = {"Products"},
      summary = "Get all products",
      description =
          "Returns a paginated list of products. "
              + "By default only returns active standard products. "
              + "Can be filtered by category using the 'categories' parameter. "
              + "Use includeSelections=true to also return special selection products. "
              + "When 'search' is provided, filters by product name, description, "
              + "and category name (partial, case-insensitive) — the filter is applied "
              + "against the full DB before pagination.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<Page<ProductResponse>> findAll(
      @Parameter(description = "Category IDs to filter by", example = "[1, 2]")
          @RequestParam(required = false)
          List<Long> categories,
      @Parameter(description = "Page number (default 0)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size (default 20, max 100)", example = "20")
          @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Include inactive products (default false)", example = "false")
          @RequestParam(defaultValue = "false")
          boolean includeInactive,
      @Parameter(
              description = "Include special selection products (default false)",
              example = "false")
          @RequestParam(defaultValue = "false")
          boolean includeSelections,
      @Parameter(
              description = "Optional name/description/category filter (partial, case-insensitive)",
              example = "burger")
          @RequestParam(required = false)
          String search) {
    if (page < 0) {
      throw new IllegalArgumentException("Page parameter must be greater than or equal to 0");
    }
    if (size <= 0 || size > 100) {
      throw new IllegalArgumentException("Size parameter must be between 1 and 100");
    }
    var pageable = PageRequest.of(page, size);
    Page<ProductResponse> responses;
    if (search != null && !search.isBlank()) {
      Page<Product> products =
          productUseCase.search(search, categories, includeInactive, includeSelections, pageable);
      responses =
          new PageImpl<>(toResponses(products.getContent()), pageable, products.getTotalElements());
    } else if (categories == null || categories.isEmpty()) {
      if (includeInactive) {
        List<Product> allProducts = productUseCase.findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProducts.size());
        List<Product> pagedContent =
            start < allProducts.size() ? allProducts.subList(start, end) : List.of();
        responses = new PageImpl<>(toResponses(pagedContent), pageable, allProducts.size());
      } else {
        Page<Product> products = productUseCase.findAllActive(pageable, includeSelections);
        responses =
            new PageImpl<>(
                toResponses(products.getContent()), pageable, products.getTotalElements());
      }
    } else {
      List<Product> filteredProducts = productUseCase.findByCategoryIds(categories);
      int start = (int) pageable.getOffset();
      int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());
      List<Product> pagedContent =
          start < filteredProducts.size() ? filteredProducts.subList(start, end) : List.of();
      responses = new PageImpl<>(toResponses(pagedContent), pageable, filteredProducts.size());
    }
    return ResponseEntity.ok(responses);
  }

  /**
   * Maps products to responses, resolving each product's primary image URL with a single batch
   * query.
   *
   * @param products the products to map
   * @return the product responses
   */
  private List<ProductResponse> toResponses(List<Product> products) {
    Map<Long, String> primaryImageUrls = resolvePrimaryImageUrls(products);
    return products.stream()
        .map(product -> ProductResponse.fromDomain(product, primaryImageUrls.get(product.getId())))
        .collect(Collectors.toList());
  }

  /**
   * Resolves the primary image URL for each product in a single batch query. The primary image is
   * the image with the lowest id for the entity.
   *
   * @param products the products to resolve images for
   * @return map of product ID to primary image signed URL
   */
  private Map<Long, String> resolvePrimaryImageUrls(Collection<Product> products) {
    if (products.isEmpty()) {
      return Map.of();
    }
    List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
    return imageRepositoryPort
        .findByEntityTypeAndEntityIds(ImageEntityType.PRODUCT, productIds)
        .stream()
        .sorted(Comparator.comparing(EntityImage::getId))
        .collect(
            Collectors.toMap(
                EntityImage::getEntityId,
                this::primaryImageUrl,
                (first, ignored) -> first,
                LinkedHashMap::new));
  }

  /**
   * Generates a signed URL for the mobile variant of an image.
   *
   * @param image the image
   * @return the signed URL
   */
  private String primaryImageUrl(EntityImage image) {
    return storagePort.generateSignedUrl(
        image.getStorageKey() + "/mobile.webp", SIGNED_URL_EXPIRATION);
  }

  /**
   * Gets top selling products.
   *
   * @return the list of top selling products
   */
  @Operation(
      tags = {"Products"},
      summary = "Get top selling products",
      description =
          "Returns the top selling products of the restaurant. "
              + "Currently returns an empty list — full analytics coming soon.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
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
      tags = {"Products"},
      summary = "Get product by ID",
      description = "Returns a specific product by its identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    Product product = productUseCase.findById(id);
    List<OptionGroup> optionGroups = optionGroupUseCase.findByProductId(id);
    product.setOptionGroupIds(optionGroups.stream().map(OptionGroup::getId).toList());
    return ResponseEntity.ok(ProductResponse.fromDomain(product));
  }

  /**
   * Disables a product.
   *
   * @param id the product ID
   * @return the disabled product
   */
  @Operation(
      tags = {"Products"},
      summary = "Disable product",
      description =
          "Performs logical deletion by setting the product as inactive. "
              + "Physical deletion is not performed.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product disabled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/disable")
  public ResponseEntity<ProductResponse> disable(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    Product product = productUseCase.disable(id);
    return ResponseEntity.ok(ProductResponse.fromDomain(product));
  }

  /**
   * Enables a product.
   *
   * @param id the product ID
   * @return the enabled product
   */
  @Operation(
      tags = {"Products"},
      summary = "Enable product",
      description = "Reactivates a previously disabled product by setting the product as active.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Product enabled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/enable")
  public ResponseEntity<ProductResponse> enable(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    Product product = productUseCase.enable(id);
    return ResponseEntity.ok(ProductResponse.fromDomain(product));
  }

  /**
   * Gets options for a product.
   *
   * @param id the product ID
   * @return the list of product options
   */
  @Operation(
      tags = {"Products"},
      summary = "Get product options",
      description = "Returns the personalization options associated with a specific product.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Options retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}/options")
  public ResponseEntity<List<ProductOptionResponse>> findOptionsByProductId(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    ProductCostBreakdown breakdown = getProductCostBreakdownUseCase.execute(id);
    List<ProductOptionResponse> responses =
        breakdown.options().stream().map(ProductOptionResponse::fromCostBreakdown).toList();
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets option groups associated with a product.
   *
   * @param id the product ID
   * @return the list of option groups with their options
   */
  @Operation(
      tags = {"Products"},
      summary = "Get product option groups",
      description =
          "Returns the option groups associated with a specific product, "
              + "with selection type and required flag.")
  @GetMapping("/{id}/option-groups")
  public ResponseEntity<List<OptionGroupResponse>> findOptionGroupsByProductId(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    List<OptionGroup> optionGroups = optionGroupUseCase.findByProductId(id);
    Map<Long, String> selectionTypes =
        optionGroupUseCase.loadSelectionTypesByIds(
            optionGroups.stream().map(OptionGroup::getId).toList());
    List<OptionGroupResponse> responses =
        optionGroups.stream()
            .map(
                og ->
                    OptionGroupResponse.fromDomain(og, selectionTypes.get(og.getId()), List.of(id)))
            .toList();
    return ResponseEntity.ok(responses);
  }

  /**
   * Calculates the on-the-fly production cost of a product.
   *
   * @param id the product ID
   * @return the calculated cost including material and labor components
   */
  @Operation(
      tags = {"Products"},
      summary = "Calculate production cost",
      description =
          "Calculates material + labor cost on-the-fly. "
              + "Material = recipe quantity * supply variant unit cost. "
              + "Labor = avg worker hourly rate * estimated prep time.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Cost calculated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}/cost")
  public ResponseEntity<ProductCostResponse> calculateCost(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    ProductCost cost = calculateProductCostUseCase.calculateCost(id);
    return ResponseEntity.ok(ProductCostResponse.fromDomain(cost));
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

  /**
   * Maps a list of {@link OptionExtrasRequest} to a domain map keyed by option id. Null or empty
   * input yields a null map (so the service treats the request as having no surcharge overrides).
   *
   * @param optionExtras the per-option surcharge requests
   * @return option-id → surcharge Money map, or null when no surcharges were supplied
   */
  private Map<Long, Money> mapOptionExtras(List<OptionExtrasRequest> optionExtras) {
    if (optionExtras == null || optionExtras.isEmpty()) {
      return null;
    }
    return optionExtras.stream()
        .collect(
            Collectors.toMap(
                OptionExtrasRequest::optionId,
                OptionExtrasRequest::toMoney,
                (first, ignored) -> first));
  }
}
