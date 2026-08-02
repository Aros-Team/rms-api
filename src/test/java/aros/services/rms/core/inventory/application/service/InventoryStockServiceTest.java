/* (C) 2026 */

package aros.services.rms.core.inventory.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.StorageLocation;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Tests {@link InventoryStockService#isAvailable(Long, List)} selection-mode semantics (Phase D).
 *
 * <ul>
 *   <li>SINGLE_CHOICE with {@code replace_supply_category_id} selected → base-recipe lines of the
 *       replaced slot are removed and the option's recipe is required instead.
 *   <li>REMOVE selected → the option's recipe is subtracted from the base.
 *   <li>SINGLE_CHOICE (no replacement), MULTI_CHOICE and EXTRA → the option's recipe is added.
 *   <li>No selection → base recipe stays intact.
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryStockServiceTest {

  private static final Long PRODUCT_ID = 1L;
  private static final Long SLOT_VARIANT = 10L; // base line that the substitution slot replaces
  private static final Long OTHER_BASE_VARIANT = 11L; // other base line
  private static final Long OPTION_VARIANT = 12L; // option recipe variant
  private static final Long EXTRA_VARIANT = 13L;

  @Mock private ProductRecipeRepositoryPort productRecipeRepositoryPort;
  @Mock private OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  @Mock private InventoryStockRepositoryPort inventoryStockRepositoryPort;
  @Mock private StorageLocationRepositoryPort storageLocationRepositoryPort;
  @Mock private ProductOptionRepositoryPort productOptionRepositoryPort;

  private InventoryStockService service;

  @BeforeEach
  void setUp() {
    service =
        new InventoryStockService(
            productRecipeRepositoryPort,
            optionRecipeRepositoryPort,
            inventoryStockRepositoryPort,
            storageLocationRepositoryPort,
            productOptionRepositoryPort);

    when(storageLocationRepositoryPort.findByName("Cocina"))
        .thenReturn(Optional.of(StorageLocation.builder().id(2L).name("Cocina").build()));
    when(storageLocationRepositoryPort.findByName("Bodega"))
        .thenReturn(Optional.of(StorageLocation.builder().id(1L).name("Bodega").build()));
  }

  private List<ProductRecipe> baseRecipes() {
    return List.of(
        ProductRecipe.builder()
            .productId(PRODUCT_ID)
            .supplyVariantId(SLOT_VARIANT)
            .requiredQuantity(new BigDecimal("2"))
            .build(),
        ProductRecipe.builder()
            .productId(PRODUCT_ID)
            .supplyVariantId(OTHER_BASE_VARIANT)
            .requiredQuantity(new BigDecimal("1"))
            .build());
  }

  private ProductOption substitutionOption() {
    OptionGroup category =
        OptionGroup.builder()
            .id(7L)
            .name("Salsa")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(500L)
            .build();
    return ProductOption.builder().id(100L).name("Salsa tártara").category(category).build();
  }

  private ProductOption removeOption() {
    OptionGroup category =
        OptionGroup.builder()
            .id(8L)
            .name("Quitar")
            .selectionType(OptionSelectionType.REMOVAL)
            .build();
    return ProductOption.builder().id(101L).name("Sin cebolla").category(category).build();
  }

  private ProductOption multiSelectOption() {
    OptionGroup category =
        OptionGroup.builder()
            .id(9L)
            .name("Adiciones")
            .selectionType(OptionSelectionType.MULTI_CHOICE)
            .build();
    return ProductOption.builder().id(102L).name("Tocino").category(category).build();
  }

  private ProductOption extraOption() {
    OptionGroup category =
        OptionGroup.builder()
            .id(10L)
            .name("Extra")
            .selectionType(OptionSelectionType.ADD_ON)
            .build();
    return ProductOption.builder().id(104L).name("Extra queso").category(category).build();
  }

  private ProductOption singleChoiceOption() {
    OptionGroup category =
        OptionGroup.builder()
            .id(11L)
            .name("Cocción")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .build();
    return ProductOption.builder().id(103L).name("Término medio").category(category).build();
  }

  private void stubOptionRecipe(Long optionId, Long variantId, BigDecimal quantity) {
    when(optionRecipeRepositoryPort.findByOptionIdIn(List.of(optionId)))
        .thenReturn(
            List.of(
                OptionRecipe.builder()
                    .optionId(optionId)
                    .supplyVariantId(variantId)
                    .requiredQuantity(quantity)
                    .build()));
  }

  private void stubSlotRecipe() {
    when(productOptionRepositoryPort.loadBaseRecipeBySupplyCategory(PRODUCT_ID, 500L))
        .thenReturn(
            List.of(
                ProductRecipe.builder()
                    .productId(PRODUCT_ID)
                    .supplyVariantId(SLOT_VARIANT)
                    .requiredQuantity(new BigDecimal("2"))
                    .build()));
  }

  private void stubStock(Long variantId, BigDecimal quantity) {
    when(inventoryStockRepositoryPort.findByVariantAndLocationWithLock(eq(variantId), anyLong()))
        .thenReturn(
            Optional.of(
                InventoryStock.builder()
                    .id(1L)
                    .supplyVariantId(variantId)
                    .storageLocationId(2L)
                    .currentQuantity(quantity)
                    .build()));
  }

  // ---------------------------------------------------------------------------
  // Sustitución (SINGLE_CHOICE con replace_supply_category_id)
  // ---------------------------------------------------------------------------

  @Test
  void shouldBeAvailable_whenSubstitutionSelected_andSlotVariantHasNoStock() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubSlotRecipe();
    stubOptionRecipe(100L, OPTION_VARIANT, new BigDecimal("3"));
    when(productOptionRepositoryPort.findAllById(List.of(100L)))
        .thenReturn(List.of(substitutionOption()));

    // La variante del slot (10) no tiene stock: si la línea base no se hubiera quitado, no habría
    // disponibilidad. La receta de la opción (12) y la otra línea base (11) sí tienen.
    stubStock(SLOT_VARIANT, BigDecimal.ZERO);
    stubStock(OPTION_VARIANT, new BigDecimal("5"));
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("5"));

    assertTrue(service.isAvailable(PRODUCT_ID, List.of(100L)));
  }

  @Test
  void shouldBeUnavailable_whenSubstitutionSelected_andOptionRecipeHasNoStock() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubSlotRecipe();
    stubOptionRecipe(100L, OPTION_VARIANT, new BigDecimal("3"));
    when(productOptionRepositoryPort.findAllById(List.of(100L)))
        .thenReturn(List.of(substitutionOption()));

    stubStock(SLOT_VARIANT, BigDecimal.ZERO);
    stubStock(OPTION_VARIANT, BigDecimal.ZERO);
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("5"));

    assertFalse(service.isAvailable(PRODUCT_ID, List.of(100L)));
  }

  @Test
  void shouldKeepBaseIntact_whenSubstitutionSlotHasNoBaseLines() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    // Slot lookup no encuentra líneas base → nada que restar → base intacta.
    when(productOptionRepositoryPort.loadBaseRecipeBySupplyCategory(PRODUCT_ID, 500L))
        .thenReturn(List.of());
    stubOptionRecipe(100L, OPTION_VARIANT, new BigDecimal("3"));
    when(productOptionRepositoryPort.findAllById(List.of(100L)))
        .thenReturn(List.of(substitutionOption()));

    stubStock(SLOT_VARIANT, BigDecimal.ZERO);
    stubStock(OPTION_VARIANT, new BigDecimal("5"));
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("5"));

    // La variante 10 sigue siendo requerida y no tiene stock → no disponible.
    assertFalse(service.isAvailable(PRODUCT_ID, List.of(100L)));
  }

  // ---------------------------------------------------------------------------
  // Sin selección → base intacta
  // ---------------------------------------------------------------------------

  @Test
  void shouldBeAvailable_whenNoSelection_andBaseStockSufficient() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());

    stubStock(SLOT_VARIANT, new BigDecimal("2"));
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("1"));

    assertTrue(service.isAvailable(PRODUCT_ID, List.of()));
  }

  @Test
  void shouldBeUnavailable_whenNoSelection_andBaseStockInsufficient() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());

    stubStock(SLOT_VARIANT, BigDecimal.ZERO);
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("1"));

    assertFalse(service.isAvailable(PRODUCT_ID, null));
  }

  // ---------------------------------------------------------------------------
  // REMOVE → resta la receta de la opción
  // ---------------------------------------------------------------------------

  @Test
  void shouldBeAvailable_whenRemoveSelected_andCancelledSlotVariantHasNoStock() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(101L, SLOT_VARIANT, new BigDecimal("2"));
    when(productOptionRepositoryPort.findAllById(List.of(101L)))
        .thenReturn(List.of(removeOption()));

    // La variante 10 queda en neto 0 (2 − 2): sin stock no importa; la 11 sí se requiere.
    stubStock(SLOT_VARIANT, BigDecimal.ZERO);
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("5"));

    assertTrue(service.isAvailable(PRODUCT_ID, List.of(101L)));
  }

  @Test
  void shouldBeUnavailable_whenRemoveSelected_andRemainingBaseLineHasNoStock() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(101L, SLOT_VARIANT, new BigDecimal("2"));
    when(productOptionRepositoryPort.findAllById(List.of(101L)))
        .thenReturn(List.of(removeOption()));

    stubStock(SLOT_VARIANT, BigDecimal.ZERO);
    stubStock(OTHER_BASE_VARIANT, BigDecimal.ZERO);

    assertFalse(service.isAvailable(PRODUCT_ID, List.of(101L)));
  }

  // ---------------------------------------------------------------------------
  // MULTI_CHOICE / EXTRA / SINGLE_CHOICE sin reemplazo → suman recetas
  // ---------------------------------------------------------------------------

  @Test
  void shouldBeUnavailable_whenMultiOrExtraSelected_andOptionVariantHasNoStock() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(102L, OPTION_VARIANT, new BigDecimal("3"));
    stubOptionRecipe(104L, EXTRA_VARIANT, new BigDecimal("1"));
    when(productOptionRepositoryPort.findAllById(List.of(102L, 104L)))
        .thenReturn(List.of(multiSelectOption(), extraOption()));

    stubStock(SLOT_VARIANT, new BigDecimal("5"));
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("5"));
    stubStock(OPTION_VARIANT, BigDecimal.ZERO);
    stubStock(EXTRA_VARIANT, new BigDecimal("5"));

    // La receta de la opción MULTI se suma y no hay stock → no disponible.
    assertFalse(service.isAvailable(PRODUCT_ID, List.of(102L, 104L)));
  }

  @Test
  void shouldBeAvailable_whenMultiAndExtraSelected_andAllStockSufficient() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(102L, OPTION_VARIANT, new BigDecimal("3"));
    stubOptionRecipe(104L, EXTRA_VARIANT, new BigDecimal("1"));
    when(productOptionRepositoryPort.findAllById(List.of(102L, 104L)))
        .thenReturn(List.of(multiSelectOption(), extraOption()));

    stubStock(SLOT_VARIANT, new BigDecimal("5"));
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("5"));
    stubStock(OPTION_VARIANT, new BigDecimal("5"));
    stubStock(EXTRA_VARIANT, new BigDecimal("5"));

    assertTrue(service.isAvailable(PRODUCT_ID, List.of(102L, 104L)));
  }

  @Test
  void shouldBeUnavailable_whenSingleChoiceWithoutReplacementSelected_andOptionVariantHasNoStock() {
    when(productRecipeRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(baseRecipes());
    stubOptionRecipe(103L, OPTION_VARIANT, new BigDecimal("3"));
    when(productOptionRepositoryPort.findAllById(List.of(103L)))
        .thenReturn(List.of(singleChoiceOption()));

    stubStock(SLOT_VARIANT, new BigDecimal("5"));
    stubStock(OTHER_BASE_VARIANT, new BigDecimal("5"));
    stubStock(OPTION_VARIANT, BigDecimal.ZERO);

    assertFalse(service.isAvailable(PRODUCT_ID, List.of(103L)));
  }
}
