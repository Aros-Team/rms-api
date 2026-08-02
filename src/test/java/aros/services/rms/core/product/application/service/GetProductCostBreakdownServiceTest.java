/* (C) 2026 */

package aros.services.rms.core.product.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductCostBreakdown;
import aros.services.rms.core.product.domain.ProductOptionCostProfile;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link GetProductCostBreakdownService}. */
@ExtendWith(MockitoExtension.class)
class GetProductCostBreakdownServiceTest {

  private static final Long PRODUCT_ID = 42L;
  private static final Currency COP = Currency.getInstance("COP");

  @Mock private ProductRepositoryPort productRepositoryPort;
  @Mock private ProductRecipeRepositoryPort productRecipeRepositoryPort;
  @Mock private SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  @Mock private ProductOptionRepositoryPort productOptionRepositoryPort;
  @Mock private OptionRecipeRepositoryPort optionRecipeRepositoryPort;

  private GetProductCostBreakdownService service;

  @BeforeEach
  void setUp() {
    service =
        new GetProductCostBreakdownService(
            productRepositoryPort,
            productRecipeRepositoryPort,
            supplyVariantRepositoryPort,
            productOptionRepositoryPort,
            optionRecipeRepositoryPort);
  }

  @Test
  void should_return_zero_costs_when_recipe_and_options_are_empty() {
    stubProduct(List.of());

    ProductCostBreakdown result = service.execute(PRODUCT_ID);

    assertMoney("0.00", result.baseCost());
    assertMoney("0.00", result.projectedOptionCost());
    assertMoney("0.00", result.projectedEffectiveCost());
    assertEquals(List.of(), result.options());
    assertEquals(List.of(), result.categories());
  }

  @Test
  void should_project_substitution_category_with_default_and_two_options() {
    List<ProductOptionCostProfile> profiles =
        List.of(
            profile(1L, "Pollo", 10L, "Proteína", "0", "SINGLE_CHOICE", 99L, "12"),
            profile(2L, "Cerdo", 10L, "Proteína", "0", "SINGLE_CHOICE", 99L, "12"));
    stubProduct(profiles);
    when(optionRecipeRepositoryPort.loadMaterialCostByOptionIds(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, money("18"), 2L, money("24")));

    ProductCostBreakdown result = service.execute(PRODUCT_ID);

    assertEquals(1, result.categories().size());
    assertMoney("12.00", result.categories().getFirst().defaultSlotCost());
    assertMoney("18.00", result.categories().getFirst().slotProjectedCost());
    assertMoney("6.00", result.categories().getFirst().projectedContribution());
    assertMoney("6.00", result.projectedOptionCost());
  }

  @Test
  void should_project_non_substitution_category_as_average_option_cost() {
    List<ProductOptionCostProfile> profiles =
        List.of(
            profile(1L, "Pequeña", 10L, "Tamaño", "0", "SINGLE_CHOICE", null, "0"),
            profile(2L, "Grande", 10L, "Tamaño", "0", "SINGLE_CHOICE", null, "0"));
    stubProduct(profiles);
    when(optionRecipeRepositoryPort.loadMaterialCostByOptionIds(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, money("10"), 2L, money("20")));

    ProductCostBreakdown result = service.execute(PRODUCT_ID);

    assertMoney("15.00", result.categories().getFirst().slotProjectedCost());
    assertMoney("15.00", result.categories().getFirst().projectedContribution());
    assertMoney("15.00", result.projectedOptionCost());
  }

  @Test
  void should_project_multi_select_category_as_average_option_cost() {
    List<ProductOptionCostProfile> profiles =
        List.of(
            profile(1L, "Queso", 10L, "Ingredientes", "0", "MULTI_CHOICE", null, "0"),
            profile(2L, "Tocineta", 10L, "Ingredientes", "0", "MULTI_CHOICE", null, "0"));
    stubProduct(profiles);
    when(optionRecipeRepositoryPort.loadMaterialCostByOptionIds(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, money("4"), 2L, money("8")));

    ProductCostBreakdown result = service.execute(PRODUCT_ID);

    assertEquals("MULTI_CHOICE", result.categories().getFirst().selectionType());
    assertMoney("6.00", result.categories().getFirst().slotProjectedCost());
    assertMoney("6.00", result.categories().getFirst().projectedContribution());
  }

  @Test
  void should_list_extra_cost_and_price_without_projecting_its_contribution() {
    ProductOptionCostProfile extra =
        profile(3L, "Queso extra", 20L, "Extras", "7", "ADD_ON", null, "0");
    stubProduct(List.of(extra));
    when(optionRecipeRepositoryPort.loadMaterialCostByOptionIds(List.of(3L)))
        .thenReturn(Map.of(3L, money("5")));

    ProductCostBreakdown result = service.execute(PRODUCT_ID);

    assertEquals(1, result.options().size());
    assertMoney("5.00", result.options().getFirst().cost());
    assertMoney("7.00", result.options().getFirst().extraPrice());
    assertEquals("ADD_ON", result.options().getFirst().categorySelectionType());
    assertMoney("0.00", result.categories().getFirst().projectedContribution());
    assertMoney("0.00", result.projectedOptionCost());
  }

  @Test
  void should_exclude_remove_category_from_projection() {
    ProductOptionCostProfile remove =
        profile(4L, "Sin cebolla", 30L, "Remover", "0", "REMOVAL", null, "0");
    stubProduct(List.of(remove));
    when(optionRecipeRepositoryPort.loadMaterialCostByOptionIds(List.of(4L)))
        .thenReturn(Map.of(4L, money("2")));

    ProductCostBreakdown result = service.execute(PRODUCT_ID);

    assertMoney("2.00", result.options().getFirst().cost());
    assertMoney("0.00", result.categories().getFirst().slotProjectedCost());
    assertMoney("0.00", result.categories().getFirst().projectedContribution());
    assertMoney("0.00", result.projectedOptionCost());
  }

  @Test
  void should_add_all_projected_contributions_to_base_material_cost() {
    Product product = Product.builder().id(PRODUCT_ID).name("Burger").build();
    ProductRecipe recipe =
        ProductRecipe.builder()
            .productId(PRODUCT_ID)
            .supplyVariantId(100L)
            .requiredQuantity(new BigDecimal("2"))
            .build();
    SupplyVariant variant = SupplyVariant.builder().id(100L).unitCost(money("4")).build();
    List<ProductOptionCostProfile> profiles =
        List.of(
            profile(1L, "A", 10L, "Tamaño", "0", "MULTI_CHOICE", null, "0"),
            profile(2L, "B", 10L, "Tamaño", "0", "MULTI_CHOICE", null, "0"),
            profile(3L, "C", 20L, "Proteína", "0", "SINGLE_CHOICE", 99L, "4"),
            profile(4L, "D", 30L, "Extra", "5", "ADD_ON", null, "0"),
            profile(5L, "E", 40L, "Remover", "0", "REMOVAL", null, "0"));

    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of(recipe));
    when(supplyVariantRepositoryPort.findAllById(List.of(100L))).thenReturn(List.of(variant));
    when(productOptionRepositoryPort.loadCostProfilesByProductId(PRODUCT_ID)).thenReturn(profiles);
    when(optionRecipeRepositoryPort.loadMaterialCostByOptionIds(List.of(1L, 2L, 3L, 4L, 5L)))
        .thenReturn(
            Map.of(
                1L, money("6"),
                2L, money("10"),
                3L, money("10"),
                4L, money("100"),
                5L, money("2")));

    ProductCostBreakdown result = service.execute(PRODUCT_ID);

    assertMoney("8.00", result.baseCost());
    assertMoney("11.00", result.projectedOptionCost());
    assertMoney("19.00", result.projectedEffectiveCost());
  }

  @Test
  void should_throw_when_product_does_not_exist() {
    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> service.execute(PRODUCT_ID));
    verifyNoInteractions(
        productRecipeRepositoryPort,
        supplyVariantRepositoryPort,
        productOptionRepositoryPort,
        optionRecipeRepositoryPort);
  }

  private void stubProduct(List<ProductOptionCostProfile> profiles) {
    Product product = Product.builder().id(PRODUCT_ID).name("Burger").build();
    when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of());
    when(productOptionRepositoryPort.loadCostProfilesByProductId(PRODUCT_ID)).thenReturn(profiles);
  }

  private static ProductOptionCostProfile profile(
      Long optionId,
      String optionName,
      Long categoryId,
      String categoryName,
      String extraPrice,
      String selectionType,
      Long replaceSupplyCategoryId,
      String defaultSlotCost) {
    return new ProductOptionCostProfile(
        optionId,
        optionName,
        categoryId,
        categoryName,
        money(extraPrice),
        selectionType,
        replaceSupplyCategoryId,
        money(defaultSlotCost));
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }

  private static void assertMoney(String expectedAmount, Money actual) {
    assertEquals(0, new BigDecimal(expectedAmount).compareTo(actual.amount()));
    assertEquals(COP, actual.currency());
  }
}
