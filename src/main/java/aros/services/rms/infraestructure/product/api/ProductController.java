/* (C) 2026 */

package aros.services.rms.infraestructure.product.api;

import aros.services.rms.core.analytics.domain.TopSellingProduct;
import aros.services.rms.core.analytics.domain.port.in.GetTopSellingProductsUseCase;
import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.product.domain.PrepTimeMode;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductCost;
import aros.services.rms.core.product.domain.ProductCostBreakdown;
import aros.services.rms.core.product.domain.ProductPrepTime;
import aros.services.rms.core.product.port.input.CalculateProductCostUseCase;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.core.product.port.input.GetProductPrepTimeUseCase;
import aros.services.rms.core.product.port.input.ProductUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import aros.services.rms.infraestructure.product.api.dto.OptionAssociationRequest;
import aros.services.rms.infraestructure.product.api.dto.OptionExtraPriceRequest;
import aros.services.rms.infraestructure.product.api.dto.OptionExtrasRequest;
import aros.services.rms.infraestructure.product.api.dto.ProductCostResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductOptionResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductPrepTimeResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductRequest;
import aros.services.rms.infraestructure.product.api.dto.ProductResponse;
import aros.services.rms.infraestructure.product.api.dto.RecipeItemRequest;
import aros.services.rms.infraestructure.product.api.dto.TopSellingProductResponse;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.share.security.JustWorkerOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
@JustWorkerOrAdmin
public class ProductController {

  private final ProductUseCase productUseCase;
  private final CalculateProductCostUseCase calculateProductCostUseCase;
  private final GetProductCostBreakdownUseCase getProductCostBreakdownUseCase;
  private final GetProductPrepTimeUseCase getProductPrepTimeUseCase;
  private final ImageRepositoryPort imageRepositoryPort;
  private final StoragePort storagePort;
  private final OptionGroupUseCase optionGroupUseCase;
  private final GetTopSellingProductsUseCase getTopSellingProductsUseCase;
  private final CurrencyProvider currencyProvider;

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
  @JustAdminUser
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .description(request.description())
            .basePrice(
                new Money(BigDecimal.valueOf(request.basePrice()), currencyProvider.getCurrency()))
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .optionIds(request.optionIds())
            .optionExtras(mapOptionExtras(request.optionExtras()))
            .recipe(recipe)
            .estimatedPrepMinutes(request.estimatedPrepMinutes())
            .requiredWorkers(request.requiredWorkers() == null ? 1 : request.requiredWorkers())
            .prepTimeMode(parsePrepTimeMode(request.prepTimeMode()))
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
  @JustAdminUser
  public ResponseEntity<ProductResponse> update(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody ProductRequest request) {
    List<ProductRecipe> recipe = mapRecipe(request.recipe());
    Product product =
        Product.builder()
            .name(request.name())
            .description(request.description())
            .basePrice(
                new Money(BigDecimal.valueOf(request.basePrice()), currencyProvider.getCurrency()))
            .category(Category.builder().id(request.categoryId()).build())
            .preparationAreaId(request.areaId())
            .optionIds(request.optionIds())
            .optionExtras(mapOptionExtras(request.optionExtras()))
            .recipe(recipe)
            .estimatedPrepMinutes(request.estimatedPrepMinutes())
            .requiredWorkers(request.requiredWorkers() == null ? 1 : request.requiredWorkers())
            .prepTimeMode(parsePrepTimeMode(request.prepTimeMode()))
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
      throw new IllegalArgumentException("El parámetro page debe ser mayor o igual a 0");
    }
    if (size <= 0 || size > 100) {
      throw new IllegalArgumentException("El parámetro size debe estar entre 1 y 100");
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
    Map<Long, PrimaryImageUrls> primaryImageUrls = resolvePrimaryImageUrls(products);
    return products.stream()
        .map(
            product -> {
              PrimaryImageUrls urls = primaryImageUrls.get(product.getId());
              return urls == null
                  ? ProductResponse.fromDomain(product)
                  : ProductResponse.fromDomain(
                      product, urls.mobile(), urls.tablet(), urls.desktop());
            })
        .collect(Collectors.toList());
  }

  /** Responsive variants of the primary image. */
  private record PrimaryImageUrls(String mobile, String tablet, String desktop) {}

  /**
   * Resolves the primary image URL for each product in a single batch query. The primary image is
   * the image with the lowest id for the entity.
   *
   * @param products the products to resolve images for
   * @return map of product ID to primary image signed URL
   */
  private Map<Long, PrimaryImageUrls> resolvePrimaryImageUrls(Collection<Product> products) {
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
                this::primaryImageUrls,
                (first, ignored) -> first,
                LinkedHashMap::new));
  }

  /**
   * Generates the signed URLs for the three responsive variants of an image.
   *
   * @param image the image
   * @return the mobile, tablet and desktop URLs
   */
  private PrimaryImageUrls primaryImageUrls(EntityImage image) {
    return new PrimaryImageUrls(
        storagePort.generateSignedUrl(
            image.getStorageKey() + "/mobile.webp", SIGNED_URL_EXPIRATION),
        storagePort.generateSignedUrl(
            image.getStorageKey() + "/tablet.webp", SIGNED_URL_EXPIRATION),
        storagePort.generateSignedUrl(
            image.getStorageKey() + "/desktop.webp", SIGNED_URL_EXPIRATION));
  }

  /**
   * Gets top selling products.
   *
   * @param from optional inclusive start date (defaults to 30 days before {@code to})
   * @param to optional inclusive end date (defaults to today)
   * @param limit maximum number of products (1..100)
   * @return the ranked products
   */
  @Operation(
      tags = {"Products"},
      summary = "Get top selling products",
      description =
          "Returns the products ranked by units sold in the requested date range, with units and"
              + " revenue. D-014: replaces the previous placeholder that always returned [].",
      responses = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "422", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/top-selling")
  @JustAdminUser
  public ResponseEntity<List<TopSellingProductResponse>> getTopSelling(
      @RequestParam(required = false)
          @org.springframework.format.annotation.DateTimeFormat(
              iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
          LocalDate from,
      @RequestParam(required = false)
          @org.springframework.format.annotation.DateTimeFormat(
              iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
          LocalDate to,
      @RequestParam(defaultValue = "10") int limit) {
    LocalDate end = to != null ? to : LocalDate.now();
    LocalDate start = from != null ? from : end.minusDays(29);
    List<TopSellingProductResponse> responses =
        getTopSellingProductsUseCase.execute(start, end, limit).stream()
            .map(ProductController::toTopSellingResponse)
            .toList();
    return ResponseEntity.ok(responses);
  }

  private static TopSellingProductResponse toTopSellingResponse(TopSellingProduct product) {
    return new TopSellingProductResponse(
        product.rank(),
        product.productId(),
        product.productName(),
        product.unitsSold(),
        MoneyDto.of(product.revenue()));
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
  @JustAdminUser
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
  @JustAdminUser
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
   * Associates an existing option with a product.
   *
   * @param id the product ID
   * @param request the option and its surcharge
   * @return 204 on success
   */
  @Operation(
      tags = {"Products"},
      summary = "Associate an option with a product",
      description =
          "Creates or updates the association between a product and an existing product option,"
              + " including the per-product surcharge. The option group is associated with the"
              + " product when it was not linked yet (legacy data).",
      responses = {
        @ApiResponse(responseCode = "204", description = "Option associated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product or option not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{id}/options")
  @JustAdminUser
  public ResponseEntity<Void> associateOption(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody OptionAssociationRequest request) {
    productUseCase.associateOption(id, request.optionId(), request.extraPrice());
    return ResponseEntity.noContent().build();
  }

  /**
   * Updates the surcharge of an option already associated with a product.
   *
   * @param id the product ID
   * @param optionId the option ID
   * @param request the new surcharge
   * @return 204 on success
   */
  @Operation(
      tags = {"Products"},
      summary = "Update an option surcharge for a product",
      responses = {
        @ApiResponse(responseCode = "204", description = "Surcharge updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Associación no encontrada"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/options/{optionId}")
  @JustAdminUser
  public ResponseEntity<Void> updateOptionExtraPrice(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
      @Parameter(description = "Option ID", example = "12", required = true) @PathVariable
          Long optionId,
      @RequestBody OptionExtraPriceRequest request) {
    productUseCase.updateOptionExtraPrice(id, optionId, request.extraPrice());
    return ResponseEntity.noContent().build();
  }

  /**
   * Removes one option association from a product.
   *
   * @param id the product ID
   * @param optionId the option ID
   * @return 204 on success
   */
  @Operation(
      tags = {"Products"},
      summary = "Detach an option from a product",
      responses = {
        @ApiResponse(responseCode = "204", description = "Option detached successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}/options/{optionId}")
  @JustAdminUser
  public ResponseEntity<Void> detachOption(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
      @Parameter(description = "Option ID", example = "12", required = true) @PathVariable
          Long optionId) {
    productUseCase.detachOption(id, optionId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Detaches an option group from a product.
   *
   * <p>Removes the product-group association and every product-option association that belongs to
   * the group. The group and its options remain in the database for future reuse.
   *
   * @param id the product ID
   * @param optionGroupId the option-group ID
   * @return 204 on success
   */
  @Operation(
      tags = {"Products"},
      summary = "Detach an option group from a product",
      description =
          "Removes the product-group link and the product's option associations for that group."
              + " The group and its options are kept in the catalogue (detach only).",
      responses = {
        @ApiResponse(responseCode = "204", description = "Option group detached successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}/option-groups/{optionGroupId}")
  @JustAdminUser
  public ResponseEntity<Void> detachOptionGroup(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
      @Parameter(description = "Option group ID", example = "4", required = true) @PathVariable
          Long optionGroupId) {
    productUseCase.detachOptionGroup(id, optionGroupId);
    return ResponseEntity.noContent().build();
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

  /**
   * Resolves the estimated and measured preparation times used for costing.
   *
   * @param id the product ID
   * @return both values plus the one currently used
   */
  @Operation(
      tags = {"Products"},
      summary = "Get product preparation times",
      description =
          "Returns the editable estimate, the measured average (when available) and which one"
              + " feeds the labor cost.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Preparation times retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}/prep-time")
  public ResponseEntity<ProductPrepTimeResponse> getPrepTime(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {
    ProductPrepTime prepTime = getProductPrepTimeUseCase.get(id);
    return ResponseEntity.ok(ProductPrepTimeResponse.fromDomain(prepTime));
  }

  private static PrepTimeMode parsePrepTimeMode(String value) {
    if (value == null || value.isBlank()) {
      return PrepTimeMode.ESTIMATED;
    }
    try {
      return PrepTimeMode.valueOf(value.trim());
    } catch (IllegalArgumentException unknown) {
      throw new IllegalArgumentException(
          "prepTimeMode inválido: '" + value + "'. Allowed values: ESTIMATED, REAL, AUTO");
    }
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
    var currency = currencyProvider.getCurrency();
    return optionExtras.stream()
        .collect(
            Collectors.toMap(
                OptionExtrasRequest::optionId,
                req -> req.toMoney(currency),
                (first, ignored) -> first));
  }
}
