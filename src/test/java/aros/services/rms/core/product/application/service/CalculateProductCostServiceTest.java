/* (C) 2026 */

package aros.services.rms.core.product.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductCost;
import aros.services.rms.core.product.domain.ProductCost.CostBreakdownItem;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link CalculateProductCostService}. */
@ExtendWith(MockitoExtension.class)
class CalculateProductCostServiceTest {

  private static final Long PRODUCT_ID = 42L;
  private static final Long AREA_ID = 7L;

  @Mock private ProductRepositoryPort productRepositoryPort;
  @Mock private ProductRecipeRepositoryPort productRecipeRepositoryPort;
  @Mock private SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  @Mock private UserRepositoryPort userRepositoryPort;
  @Mock private Logger logger;

  private CalculateProductCostService service;

  @BeforeEach
  void setUp() {
    service =
        new CalculateProductCostService(
            productRepositoryPort,
            productRecipeRepositoryPort,
            supplyVariantRepositoryPort,
            userRepositoryPort,
            logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: shouldReturnZeroCost_whenProductHasNoRecipeAndNoPrepTime
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnZeroCost_whenProductHasNoRecipeAndNoPrepTime() {
    Product product =
        Product.builder()
            .id(PRODUCT_ID)
            .name("Tea")
            .estimatedPrepMinutes(null)
            .preparationAreaId(null)
            .build();

    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of());

    ProductCost cost = service.calculateCost(PRODUCT_ID);

    assertEquals(PRODUCT_ID, cost.productId());
    assertEquals(0, cost.totalCost().compareTo(BigDecimal.ZERO));
    assertEquals(0, cost.materialCost().compareTo(BigDecimal.ZERO));
    assertEquals(0, cost.laborCost().compareTo(BigDecimal.ZERO));
    assertNotNull(cost.breakdown());
    assertTrue(cost.breakdown().isEmpty());
  }

  // ---------------------------------------------------------------------------
  // UC-02: shouldSumMaterialCost_whenAllVariantsHaveUnitCost
  // ---------------------------------------------------------------------------

  @Test
  void shouldSumMaterialCost_whenAllVariantsHaveUnitCost() {
    Product product =
        Product.builder()
            .id(PRODUCT_ID)
            .name("Burger")
            .estimatedPrepMinutes(null)
            .preparationAreaId(null)
            .build();

    List<ProductRecipe> recipes =
        List.of(
            ProductRecipe.builder()
                .id(1L)
                .productId(PRODUCT_ID)
                .supplyVariantId(10L)
                .requiredQuantity(new BigDecimal("2"))
                .build(),
            ProductRecipe.builder()
                .id(2L)
                .productId(PRODUCT_ID)
                .supplyVariantId(11L)
                .requiredQuantity(new BigDecimal("1"))
                .build(),
            ProductRecipe.builder()
                .id(3L)
                .productId(PRODUCT_ID)
                .supplyVariantId(12L)
                .requiredQuantity(new BigDecimal("3"))
                .build());

    List<SupplyVariant> variants =
        List.of(
            SupplyVariant.builder().id(10L).unitCost(new BigDecimal("1000")).build(),
            SupplyVariant.builder().id(11L).unitCost(new BigDecimal("500")).build(),
            SupplyVariant.builder().id(12L).unitCost(new BigDecimal("200")).build());

    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(recipes);
    when(supplyVariantRepositoryPort.findAllById(List.of(10L, 11L, 12L))).thenReturn(variants);

    ProductCost cost = service.calculateCost(PRODUCT_ID);

    // 2*1000 + 1*500 + 3*200 = 2000 + 500 + 600 = 3100
    assertEquals(0, new BigDecimal("3100.00").compareTo(cost.materialCost()));
    assertEquals(0, cost.materialCost().compareTo(cost.totalCost()));
    assertEquals(0, BigDecimal.ZERO.compareTo(cost.laborCost()));
    assertEquals(3, cost.breakdown().size());
  }

  // ---------------------------------------------------------------------------
  // UC-03: shouldAddLaborCost_whenWorkersArePaidInPreparationArea
  // ---------------------------------------------------------------------------

  @Test
  void shouldAddLaborCost_whenWorkersArePaidInPreparationArea() {
    Product product =
        Product.builder()
            .id(PRODUCT_ID)
            .name("Pizza")
            .estimatedPrepMinutes(30)
            .preparationAreaId(AREA_ID)
            .build();

    List<ProductRecipe> recipes =
        List.of(
            ProductRecipe.builder()
                .id(1L)
                .productId(PRODUCT_ID)
                .supplyVariantId(10L)
                .requiredQuantity(new BigDecimal("2"))
                .build());

    List<SupplyVariant> variants =
        List.of(SupplyVariant.builder().id(10L).unitCost(new BigDecimal("1000")).build());

    User worker = buildWorker(99L, new BigDecimal("1600000"));

    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(recipes);
    when(supplyVariantRepositoryPort.findAllById(List.of(10L))).thenReturn(variants);
    when(userRepositoryPort.findActiveByAreaId(AREA_ID)).thenReturn(List.of(worker));

    ProductCost cost = service.calculateCost(PRODUCT_ID);

    // material = 2 * 1000 = 2000
    // hourly = 1600000 / 160 = 10000
    // hours = 30 / 60 = 0.5
    // labor = 10000 * 0.5 = 5000
    assertEquals(0, new BigDecimal("2000.00").compareTo(cost.materialCost()));
    assertEquals(0, new BigDecimal("5000.00").compareTo(cost.laborCost()));
    assertEquals(0, new BigDecimal("7000.00").compareTo(cost.totalCost()));
    // breakdown: 1 material line + 1 labor line
    assertEquals(2, cost.breakdown().size());
    assertTrue(
        cost.breakdown().stream().anyMatch(b -> "LABOR".equals(b.type())),
        "expected a LABOR breakdown line");
  }

  // ---------------------------------------------------------------------------
  // UC-04: shouldReturnZeroLabor_whenNoWorkersInArea
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnZeroLabor_whenNoWorkersInArea() {
    Product product =
        Product.builder()
            .id(PRODUCT_ID)
            .name("Salad")
            .estimatedPrepMinutes(30)
            .preparationAreaId(AREA_ID)
            .build();

    List<ProductRecipe> recipes =
        List.of(
            ProductRecipe.builder()
                .id(1L)
                .productId(PRODUCT_ID)
                .supplyVariantId(10L)
                .requiredQuantity(new BigDecimal("1"))
                .build());

    List<SupplyVariant> variants =
        List.of(SupplyVariant.builder().id(10L).unitCost(new BigDecimal("500")).build());

    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(recipes);
    when(supplyVariantRepositoryPort.findAllById(List.of(10L))).thenReturn(variants);
    when(userRepositoryPort.findActiveByAreaId(AREA_ID)).thenReturn(List.of());

    ProductCost cost = service.calculateCost(PRODUCT_ID);

    assertEquals(0, new BigDecimal("500.00").compareTo(cost.materialCost()));
    assertEquals(0, BigDecimal.ZERO.compareTo(cost.laborCost()));
    assertEquals(0, cost.materialCost().compareTo(cost.totalCost()));
    // breakdown: 1 material line + 1 labor line (labor amount is zero but the line is recorded)
    assertEquals(2, cost.breakdown().size());
    long laborLines = cost.breakdown().stream().filter(b -> "LABOR".equals(b.type())).count();
    assertEquals(1, laborLines, "expected a LABOR breakdown line even when no workers");
    CostBreakdownItem laborLine =
        cost.breakdown().stream().filter(b -> "LABOR".equals(b.type())).findFirst().orElseThrow();
    assertEquals(0, BigDecimal.ZERO.compareTo(laborLine.amount()));
  }

  // ---------------------------------------------------------------------------
  // UC-05: shouldReturnZeroLabor_whenWorkersHaveNoSalary
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnZeroLabor_whenWorkersHaveNoSalary() {
    Product product =
        Product.builder()
            .id(PRODUCT_ID)
            .name("Soup")
            .estimatedPrepMinutes(30)
            .preparationAreaId(AREA_ID)
            .build();

    User unpaidWorker = buildWorker(99L, null);

    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of());
    when(userRepositoryPort.findActiveByAreaId(AREA_ID)).thenReturn(List.of(unpaidWorker));

    ProductCost cost = service.calculateCost(PRODUCT_ID);

    assertEquals(0, BigDecimal.ZERO.compareTo(cost.laborCost()));
    assertEquals(0, BigDecimal.ZERO.compareTo(cost.materialCost()));
    assertEquals(0, BigDecimal.ZERO.compareTo(cost.totalCost()));
  }

  // ---------------------------------------------------------------------------
  // UC-06: shouldThrowProductNotFound_whenProductDoesNotExist
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowProductNotFound_whenProductDoesNotExist() {
    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> service.calculateCost(PRODUCT_ID));
  }

  // ---------------------------------------------------------------------------
  // UC-07: shouldRecordNoUnitCostBreakdown_whenVariantMissingUnitCost
  // ---------------------------------------------------------------------------

  @Test
  void shouldRecordNoUnitCostBreakdown_whenVariantMissingUnitCost() {
    Product product =
        Product.builder()
            .id(PRODUCT_ID)
            .name("Mystery Dish")
            .estimatedPrepMinutes(null)
            .preparationAreaId(null)
            .build();

    // recipe references variant 20 — but no variant is returned by the repository,
    // so unitCost lookup falls into the "no unit cost" branch
    List<ProductRecipe> recipes =
        List.of(
            ProductRecipe.builder()
                .id(1L)
                .productId(PRODUCT_ID)
                .supplyVariantId(20L)
                .requiredQuantity(new BigDecimal("2"))
                .build());

    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(recipes);
    when(supplyVariantRepositoryPort.findAllById(List.of(20L))).thenReturn(List.of());

    ProductCost cost = service.calculateCost(PRODUCT_ID);

    // variant contributes 0 to materialCost because no unit cost was resolvable
    assertEquals(0, BigDecimal.ZERO.compareTo(cost.materialCost()));
    assertEquals(0, BigDecimal.ZERO.compareTo(cost.totalCost()));
    // breakdown still records the line as "no unit cost"
    assertEquals(1, cost.breakdown().size());
    CostBreakdownItem line = cost.breakdown().get(0);
    assertTrue(
        line.description().contains("no unit cost"),
        "expected breakdown to mark missing unit cost, was: " + line.description());
    assertEquals(0, BigDecimal.ZERO.compareTo(line.amount()));
    assertEquals("MATERIAL", line.type());
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static User buildWorker(Long id, BigDecimal salary) {
    User user =
        new User(
            UserId.of(id),
            "1000000000",
            "Worker " + id,
            new UserEmail("worker" + id + "@example.com"),
            "encoded",
            "Address",
            "555-0000",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of(AreaId.of(AREA_ID)));
    if (salary != null) {
      user.setSalary(Salary.of(salary));
    }
    return user;
  }
}
