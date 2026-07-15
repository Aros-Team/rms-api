package aros.services.rms.core.specialselection.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SuggestedPrice;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link SpecialSelectionPricingService}. */
@ExtendWith(MockitoExtension.class)
class SpecialSelectionPricingServiceTest {

  private static final Long PRODUCT_ID_A = 1L;
  private static final Long PRODUCT_ID_B = 2L;
  private static final Long BASE_PRODUCT_ID = 100L;
  private static final Long VARIANT_10 = 10L;
  private static final Long VARIANT_20 = 20L;

  @Mock private ProductRecipeRepositoryPort productRecipeRepositoryPort;
  @Mock private SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  @Mock private ProductRepositoryPort productRepositoryPort;

  private SpecialSelectionPricingService service;

  @BeforeEach
  void setUp() {
    service =
        new SpecialSelectionPricingService(
            productRecipeRepositoryPort, supplyVariantRepositoryPort, productRepositoryPort);
  }

  // ---------------------------------------------------------------------------
  // UC-01: shouldEnrichBreakdownWithProductNames_whenProductsExist
  // ---------------------------------------------------------------------------

  @Test
  void shouldEnrichBreakdownWithProductNames_whenProductsExist() {
    SpecialSelectionConfiguration config = buildConfig(false);

    ProductRecipe recipeA =
        ProductRecipe.builder()
            .id(1L)
            .productId(PRODUCT_ID_A)
            .supplyVariantId(VARIANT_10)
            .requiredQuantity(new BigDecimal("2"))
            .build();
    ProductRecipe recipeB =
        ProductRecipe.builder()
            .id(2L)
            .productId(PRODUCT_ID_B)
            .supplyVariantId(VARIANT_20)
            .requiredQuantity(new BigDecimal("1"))
            .build();

    SupplyVariant variant10 =
        SupplyVariant.builder()
            .id(VARIANT_10)
            .unitCost(new Money(new BigDecimal("500"), Currency.getInstance("COP")))
            .build();
    SupplyVariant variant20 =
        SupplyVariant.builder()
            .id(VARIANT_20)
            .unitCost(new Money(new BigDecimal("300"), Currency.getInstance("COP")))
            .build();

    Product productA = Product.builder().id(PRODUCT_ID_A).name("Hamburger").build();
    Product productB = Product.builder().id(PRODUCT_ID_B).name("French Fries").build();

    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID_A)).thenReturn(List.of(recipeA));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID_B)).thenReturn(List.of(recipeB));
    when(supplyVariantRepositoryPort.findAllById(any())).thenReturn(List.of(variant10, variant20));
    when(productRepositoryPort.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productA));
    when(productRepositoryPort.findById(PRODUCT_ID_B)).thenReturn(Optional.of(productB));

    SuggestedPrice result = service.suggestPrice(config, new BigDecimal("30"));

    assertNotNull(result);
    assertNotNull(result.getBreakdown());
    assertEquals(2, result.getBreakdown().size());

    SuggestedPrice.CostBreakdownItem itemA =
        result.getBreakdown().stream()
            .filter(b -> PRODUCT_ID_A.equals(b.getProductId()))
            .findFirst()
            .orElseThrow();
    assertEquals("Hamburger", itemA.getName());
    assertEquals(PRODUCT_ID_A, itemA.getProductId());
    assertEquals(0, new BigDecimal("1000.00").compareTo(itemA.getCost()));

    SuggestedPrice.CostBreakdownItem itemB =
        result.getBreakdown().stream()
            .filter(b -> PRODUCT_ID_B.equals(b.getProductId()))
            .findFirst()
            .orElseThrow();
    assertEquals("French Fries", itemB.getName());
    assertEquals(PRODUCT_ID_B, itemB.getProductId());
    assertEquals(0, new BigDecimal("300.00").compareTo(itemB.getCost()));
  }

  // ---------------------------------------------------------------------------
  // UC-02: shouldFallbackToProductIdString_whenProductNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldFallbackToProductIdString_whenProductNotFound() {
    SpecialSelectionConfiguration config = buildConfig(false);

    ProductRecipe recipe =
        ProductRecipe.builder()
            .id(1L)
            .productId(PRODUCT_ID_A)
            .supplyVariantId(VARIANT_10)
            .requiredQuantity(new BigDecimal("1"))
            .build();

    SupplyVariant variant =
        SupplyVariant.builder()
            .id(VARIANT_10)
            .unitCost(new Money(new BigDecimal("500"), Currency.getInstance("COP")))
            .build();

    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID_A)).thenReturn(List.of(recipe));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID_B)).thenReturn(List.of());
    when(supplyVariantRepositoryPort.findAllById(any())).thenReturn(List.of(variant));
    when(productRepositoryPort.findById(PRODUCT_ID_A)).thenReturn(Optional.empty());

    SuggestedPrice result = service.suggestPrice(config, new BigDecimal("30"));

    assertEquals(1, result.getBreakdown().size());
    assertEquals("product:" + PRODUCT_ID_A, result.getBreakdown().get(0).getName());
    assertEquals(PRODUCT_ID_A, result.getBreakdown().get(0).getProductId());
  }

  // ---------------------------------------------------------------------------
  // UC-03: shouldIncludeBaseRecipeBreakdown_whenBaseRecipeEnabled
  // ---------------------------------------------------------------------------

  @Test
  void shouldIncludeBaseRecipeBreakdown_whenBaseRecipeEnabled() {
    SpecialSelectionConfiguration config = buildConfig(true);

    ProductRecipe groupRecipe =
        ProductRecipe.builder()
            .id(1L)
            .productId(PRODUCT_ID_A)
            .supplyVariantId(VARIANT_10)
            .requiredQuantity(new BigDecimal("1"))
            .build();
    ProductRecipe baseRecipe =
        ProductRecipe.builder()
            .id(2L)
            .productId(BASE_PRODUCT_ID)
            .supplyVariantId(VARIANT_20)
            .requiredQuantity(new BigDecimal("3"))
            .build();

    SupplyVariant variant10 =
        SupplyVariant.builder()
            .id(VARIANT_10)
            .unitCost(new Money(new BigDecimal("100"), Currency.getInstance("COP")))
            .build();
    SupplyVariant variant20 =
        SupplyVariant.builder()
            .id(VARIANT_20)
            .unitCost(new Money(new BigDecimal("50"), Currency.getInstance("COP")))
            .build();

    Product groupProduct = Product.builder().id(PRODUCT_ID_A).name("Patty").build();
    Product baseProduct = Product.builder().id(BASE_PRODUCT_ID).name("Burger Combo").build();

    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID_A))
        .thenReturn(List.of(groupRecipe));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID_B)).thenReturn(List.of());
    when(productRecipeRepositoryPort.findByProductId(BASE_PRODUCT_ID))
        .thenReturn(List.of(baseRecipe));
    when(supplyVariantRepositoryPort.findAllById(any())).thenReturn(List.of(variant10, variant20));
    when(productRepositoryPort.findById(PRODUCT_ID_A)).thenReturn(Optional.of(groupProduct));
    when(productRepositoryPort.findById(BASE_PRODUCT_ID)).thenReturn(Optional.of(baseProduct));

    SuggestedPrice result = service.suggestPrice(config, new BigDecimal("20"));

    assertEquals(2, result.getBreakdown().size());
    long baseLines = result.getBreakdown().stream().filter(b -> b.getProductId() == null).count();
    assertEquals(1, baseLines, "expected one base recipe line");

    SuggestedPrice.CostBreakdownItem baseItem =
        result.getBreakdown().stream()
            .filter(b -> b.getProductId() == null)
            .findFirst()
            .orElseThrow();
    assertTrue(baseItem.getName().contains("Burger Combo"));
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private SpecialSelectionConfiguration buildConfig(boolean baseRecipeEnabled) {
    return SpecialSelectionConfiguration.builder()
        .productId(BASE_PRODUCT_ID)
        .name("Test Combo")
        .baseRecipeEnabled(baseRecipeEnabled)
        .groups(
            List.of(
                SpecialSelectionGroup.builder()
                    .id(1L)
                    .categoryId(10L)
                    .productIds(List.of(PRODUCT_ID_A, PRODUCT_ID_B))
                    .build()))
        .build();
  }
}
