/* (C) 2026 */

package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.application.exception.SingleChoiceCategoryLimitException;
import aros.services.rms.core.order.application.service.TakeOrderService;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.domain.ProductOptionCostProfile;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotAvailableException;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionAvailabilityService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionPricingService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionValidator;
import aros.services.rms.core.specialselection.domain.SelectionType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TakeOrderUseCaseImplTest {

  @Mock private OrderRepositoryPort orderRepositoryPort;

  @Mock private TableRepositoryPort tableRepositoryPort;

  @Mock private ProductRepositoryPort productRepositoryPort;

  @Mock private ProductOptionRepositoryPort productOptionRepositoryPort;

  @Mock private SpecialSelectionRepositoryPort specialSelectionRepositoryPort;

  @Mock private SpecialSelectionValidator specialSelectionValidator;

  @Mock private SpecialSelectionPricingService specialSelectionPricingService;

  @Mock private SpecialSelectionAvailabilityService specialSelectionAvailabilityService;

  @Mock private InventoryStockUseCase inventoryStockUseCase;

  @Mock private InventoryMovementUseCase inventoryMovementUseCase;

  @Mock private BusinessMetricsPort metricsPort;

  private TakeOrderService takeOrderUseCase;

  @BeforeEach
  void setUp() {
    when(inventoryStockUseCase.isAvailable(any(), any())).thenReturn(true);

    takeOrderUseCase =
        new TakeOrderService(
            orderRepositoryPort,
            tableRepositoryPort,
            productRepositoryPort,
            productOptionRepositoryPort,
            inventoryStockUseCase,
            inventoryMovementUseCase,
            metricsPort,
            specialSelectionRepositoryPort,
            specialSelectionValidator,
            specialSelectionPricingService,
            specialSelectionAvailabilityService);
  }

  @Test
  void shouldTakeOrderSuccessfully_whenProductHasOptionsAndOptionsProvided() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    ProductOption option = ProductOption.builder().id(1L).name("Extra Cheese").build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(1L))).thenReturn(List.of(option));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 1L)).thenReturn(true);
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions("No onions")
                        .selectedOptionIds(List.of(1L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    assertNotNull(result);
    assertEquals(1, result.getDetails().size());
    assertEquals(1, result.getDetails().get(0).getSelectedOptions().size());
    verify(tableRepositoryPort, times(1)).save(table);
    assertEquals(TableStatus.OCCUPIED, table.getStatus());
  }

  @Test
  void shouldTakeOrderSuccessfully_whenProductHasNoOptionsProvided() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Water")
            .basePrice(new Money(BigDecimal.valueOf(2.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Drinks").build())
            .build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(null)
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    assertNotNull(result);
    assertEquals(1, result.getDetails().size());
    assertEquals(0, result.getDetails().get(0).getSelectedOptions().size());
    verify(tableRepositoryPort, times(1)).save(table);
    assertEquals(TableStatus.OCCUPIED, table.getStatus());
  }

  @Test
  void shouldTakeOrderSuccessfully_whenProductReceivesOptionsWithoutRestriction() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Pizza Pepperoni")
            .basePrice(new Money(BigDecimal.valueOf(20.0), Currency.getInstance("COP")))
            .category(Category.builder().id(2L).name("Pizzas").build())
            .build();
    ProductOption option = ProductOption.builder().id(7L).name("Personal").build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(7L))).thenReturn(List.of(option));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 7L)).thenReturn(true);
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(7L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    assertNotNull(result);
    assertEquals(1, result.getDetails().size());
    assertEquals(1, result.getDetails().get(0).getSelectedOptions().size());
    assertEquals(TableStatus.OCCUPIED, table.getStatus());
  }

  @Test
  void shouldThrowException_whenTableNotFound() {
    when(tableRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

    TakeOrderCommand command = TakeOrderCommand.builder().tableId(99L).details(List.of()).build();

    assertThrows(IllegalArgumentException.class, () -> takeOrderUseCase.execute(command));
  }

  @Test
  void shouldThrowException_whenTableIsNotAvailable() {
    Table table = Table.builder().id(1L).status(TableStatus.OCCUPIED).build();
    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));

    TakeOrderCommand command = TakeOrderCommand.builder().tableId(1L).details(List.of()).build();

    assertThrows(IllegalStateException.class, () -> takeOrderUseCase.execute(command));
  }

  @Test
  void shouldThrowAndReleaseTable_whenProductNotFound() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());
    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(99L)
                        .instructions(null)
                        .selectedOptionIds(null)
                        .build()))
            .build();

    assertThrows(IllegalArgumentException.class, () -> takeOrderUseCase.execute(command));
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(tableRepositoryPort, times(2)).save(table);
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowAndReleaseTable_whenOptionIsNotValidForProduct() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product burger =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    Product beverage =
        Product.builder()
            .id(2L)
            .name("Beverage")
            .basePrice(new Money(BigDecimal.valueOf(5.0), Currency.getInstance("COP")))
            .category(Category.builder().id(2L).name("Drinks").build())
            .build();

    // Opción válida para beverage, no para burger
    ProductOption invalidOption = ProductOption.builder().id(99L).name("Size Large").build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(burger));
    when(productOptionRepositoryPort.findAllById(List.of(99L))).thenReturn(List.of(invalidOption));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(99L))
                        .build()))
            .build();

    InvalidProductOptionException exception =
        assertThrows(InvalidProductOptionException.class, () -> takeOrderUseCase.execute(command));

    assertEquals("Option 99 is not valid for product 1", exception.getMessage());
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(tableRepositoryPort, times(2)).save(table);
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldTakeOrderSuccessfully_whenAllOptionsAreValidForProduct() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    ProductOption option1 = ProductOption.builder().id(1L).name("Extra Cheese").build();
    ProductOption option2 = ProductOption.builder().id(2L).name("Bacon").build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(option1, option2));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 1L)).thenReturn(true);
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 2L)).thenReturn(true);

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions("Well done")
                        .selectedOptionIds(List.of(1L, 2L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    assertNotNull(result);
    assertEquals(1, result.getDetails().size());
    assertEquals(2, result.getDetails().get(0).getSelectedOptions().size());
    verify(tableRepositoryPort, times(1)).save(table);
    assertEquals(TableStatus.OCCUPIED, table.getStatus());
  }

  @Test
  void shouldThrowAndReleaseTable_whenInsufficientStock() {
    // Arrange: mesa disponible, producto existente, pero sin stock
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    // isAvailable retorna false → sin stock
    when(inventoryStockUseCase.isAvailable(any(), any())).thenReturn(false);

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(null)
                        .build()))
            .build();

    // Act & Assert: debe lanzar InsufficientStockException
    assertThrows(InsufficientStockException.class, () -> takeOrderUseCase.execute(command));

    // La mesa debe haber sido restaurada a AVAILABLE
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    // save llamado 2 veces: 1 para OCCUPIED, 1 para restaurar a AVAILABLE
    verify(tableRepositoryPort, times(2)).save(table);
    // La orden nunca debe haberse persistido
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldOccupyTable_whenOrderIsCreated() {
    // Arrange: mesa disponible, producto con stock suficiente
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Pizza")
            .basePrice(new Money(BigDecimal.valueOf(15.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(inventoryStockUseCase.isAvailable(any(), any())).thenReturn(true);
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(null)
                        .build()))
            .build();

    // Act
    takeOrderUseCase.execute(command);

    // Assert: capturar el argumento con el que se llamó tableRepositoryPort.save()
    ArgumentCaptor<Table> tableCaptor = ArgumentCaptor.forClass(Table.class);
    // save se llama exactamente una vez (flujo exitoso)
    verify(tableRepositoryPort, times(1)).save(tableCaptor.capture());
    assertEquals(TableStatus.OCCUPIED, tableCaptor.getValue().getStatus());
  }

  @Test
  void shouldDeductInventory_whenOrderIsCreated() {
    // Arrange: flujo completamente exitoso
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Pasta")
            .basePrice(new Money(BigDecimal.valueOf(12.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(inventoryStockUseCase.isAvailable(any(), any())).thenReturn(true);
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(null)
                        .build()))
            .build();

    // Act
    takeOrderUseCase.execute(command);

    // Assert: deductForOrder debe haberse invocado exactamente una vez
    verify(inventoryMovementUseCase, times(1)).deductForOrder(any(), any());
  }

  @Test
  void shouldThrowSpecialSelectionUnavailable_whenComboOutsideActiveSchedule() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(10L)
            .name("Combo Meal")
            .basePrice(new Money(BigDecimal.valueOf(25.0), Currency.getInstance("COP")))
            .selectionType(SelectionType.SPECIAL_SELECTION)
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    SpecialSelectionConfiguration config =
        SpecialSelectionConfiguration.builder()
            .productId(10L)
            .active(true)
            .schedulingRequired(true)
            .build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(10L)).thenReturn(Optional.of(product));
    when(specialSelectionRepositoryPort.findById(10L)).thenReturn(Optional.of(config));
    when(specialSelectionAvailabilityService.isAvailable(any(), any())).thenReturn(false);

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(10L)
                        .instructions(null)
                        .selectedOptionIds(null)
                        .selectedProductIds(List.of(1L, 2L))
                        .build()))
            .build();

    SpecialSelectionNotAvailableException exception =
        assertThrows(
            SpecialSelectionNotAvailableException.class, () -> takeOrderUseCase.execute(command));

    assertEquals(10L, exception.getProductId());
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  // ---------------------------------------------------------------------------
  // Phase C — extra pricing + SINGLE_CHOICE max-1.
  // ---------------------------------------------------------------------------

  @Test
  void should_keepUnitPriceAtBasePrice_when_noExtraOptionIsSelected() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    OptionCategory cheeseCat =
        OptionCategory.builder()
            .id(50L)
            .name("Queso")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .build();
    ProductOption cheese =
        ProductOption.builder().id(5L).name("Cheddar").category(cheeseCat).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(5L))).thenReturn(List.of(cheese));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 5L)).thenReturn(true);
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(5L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    assertNotNull(result);
    OrderDetail detail = result.getDetails().get(0);
    // A SINGLE_CHOICE selection does not add to unitPrice: unitPrice stays at the basePrice.
    assertEquals(10.0, detail.getUnitPrice().amount().doubleValue(), 0.001);
    // singleton Cheese is non-EXTRA; extraCharge must remain zero.
    assertEquals(0.0, detail.getExtraCharge().amount().doubleValue(), 0.001);
  }

  @Test
  void should_computeUnitPriceAsBasePlusExtraSurcharge_when_singleExtraOptionSelected() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    OptionCategory extrasCat =
        OptionCategory.builder()
            .id(60L)
            .name("Adición")
            .selectionType(OptionSelectionType.EXTRA)
            .build();
    ProductOption extraCheese =
        ProductOption.builder().id(7L).name("Extra Cheese").category(extrasCat).build();
    Money extraPrice = new Money(BigDecimal.valueOf(2.5), Currency.getInstance("COP"));

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(7L))).thenReturn(List.of(extraCheese));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 7L)).thenReturn(true);
    when(productOptionRepositoryPort.loadCostProfilesByProductId(1L))
        .thenReturn(
            List.of(
                new ProductOptionCostProfile(
                    7L,
                    "Extra Cheese",
                    60L,
                    "Adición",
                    extraPrice,
                    OptionSelectionType.EXTRA.name(),
                    null,
                    Money.zero(Currency.getInstance("COP")))));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(7L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    OrderDetail detail = result.getDetails().get(0);
    assertEquals(12.5, detail.getUnitPrice().amount().doubleValue(), 0.001);
    assertEquals(2.5, detail.getExtraCharge().amount().doubleValue(), 0.001);
    assertEquals(extraPrice, detail.getOptionExtraPrices().get(7L));
  }

  @Test
  void should_sumOnlyExtraOptionSurcharges_intoUnitPrice_when_mixedCategoryOptions() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    OptionCategory proteinCat =
        OptionCategory.builder()
            .id(70L)
            .name("Proteína")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .build();
    OptionCategory extrasCat =
        OptionCategory.builder()
            .id(60L)
            .name("Adición")
            .selectionType(OptionSelectionType.EXTRA)
            .build();
    ProductOption protein =
        ProductOption.builder().id(8L).name("Pollo").category(proteinCat).build();
    ProductOption extraCheese =
        ProductOption.builder().id(9L).name("Extra Cheese").category(extrasCat).build();
    ProductOption extraBacon =
        ProductOption.builder().id(10L).name("Extra Bacon").category(extrasCat).build();

    Money proteinPrice = new Money(BigDecimal.valueOf(13.0), Currency.getInstance("COP"));
    Money cheesePrice = new Money(BigDecimal.valueOf(2.5), Currency.getInstance("COP"));
    Money baconPrice = new Money(BigDecimal.valueOf(3.0), Currency.getInstance("COP"));

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(8L, 9L, 10L)))
        .thenReturn(List.of(protein, extraCheese, extraBacon));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 8L)).thenReturn(true);
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 9L)).thenReturn(true);
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 10L)).thenReturn(true);
    when(productOptionRepositoryPort.loadCostProfilesByProductId(1L))
        .thenReturn(
            List.of(
                new ProductOptionCostProfile(
                    8L,
                    "Pollo",
                    70L,
                    "Proteína",
                    proteinPrice,
                    OptionSelectionType.SINGLE_CHOICE.name(),
                    null,
                    Money.zero(Currency.getInstance("COP"))),
                new ProductOptionCostProfile(
                    9L,
                    "Extra Cheese",
                    60L,
                    "Adición",
                    cheesePrice,
                    OptionSelectionType.EXTRA.name(),
                    null,
                    Money.zero(Currency.getInstance("COP"))),
                new ProductOptionCostProfile(
                    10L,
                    "Extra Bacon",
                    60L,
                    "Adición",
                    baconPrice,
                    OptionSelectionType.EXTRA.name(),
                    null,
                    Money.zero(Currency.getInstance("COP")))));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(8L, 9L, 10L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    OrderDetail detail = result.getDetails().get(0);
    // basePrice = 10, EXTRA extras = 2.5 + 3.0 = 5.5; SINGLE_CHOICE protein does NOT contribute.
    assertEquals(15.5, detail.getUnitPrice().amount().doubleValue(), 0.001);
    assertEquals(5.5, detail.getExtraCharge().amount().doubleValue(), 0.001);
    assertEquals(3, detail.getOptionExtraPrices().size());
    assertEquals(proteinPrice, detail.getOptionExtraPrices().get(8L));
    assertEquals(cheesePrice, detail.getOptionExtraPrices().get(9L));
    assertEquals(baconPrice, detail.getOptionExtraPrices().get(10L));
  }

  @Test
  void should_throwSingleChoiceCategoryLimitException_when_moreThanOneFromSingleChoiceCategory() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    OptionCategory proteinCat =
        OptionCategory.builder()
            .id(70L)
            .name("Proteína")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .build();
    ProductOption chicken =
        ProductOption.builder().id(1L).name("Pollo").category(proteinCat).build();
    ProductOption beef = ProductOption.builder().id(2L).name("Carne").category(proteinCat).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(chicken, beef));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 1L)).thenReturn(true);
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 2L)).thenReturn(true);

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(1L, 2L))
                        .build()))
            .build();

    SingleChoiceCategoryLimitException exception =
        assertThrows(
            SingleChoiceCategoryLimitException.class, () -> takeOrderUseCase.execute(command));

    assertEquals(70L, exception.getCategoryId());
    assertEquals(2, exception.getSelectedCount());
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void should_allowMultipleSelections_fromMultiSelectCategory() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Pizza")
            .basePrice(new Money(BigDecimal.valueOf(15.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Pizzas").build())
            .build();
    OptionCategory toppingsCat =
        OptionCategory.builder()
            .id(80L)
            .name("Toppings")
            .selectionType(OptionSelectionType.MULTI_SELECT)
            .build();
    ProductOption mushrooms =
        ProductOption.builder().id(11L).name("Hongos").category(toppingsCat).build();
    ProductOption olives =
        ProductOption.builder().id(12L).name("Aceitunas").category(toppingsCat).build();
    Money zero = Money.zero(Currency.getInstance("COP"));

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(11L, 12L)))
        .thenReturn(List.of(mushrooms, olives));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 11L)).thenReturn(true);
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 12L)).thenReturn(true);
    when(productOptionRepositoryPort.loadCostProfilesByProductId(1L))
        .thenReturn(
            List.of(
                new ProductOptionCostProfile(
                    11L,
                    "Hongos",
                    80L,
                    "Toppings",
                    zero,
                    OptionSelectionType.MULTI_SELECT.name(),
                    null,
                    zero),
                new ProductOptionCostProfile(
                    12L,
                    "Aceitunas",
                    80L,
                    "Toppings",
                    zero,
                    OptionSelectionType.MULTI_SELECT.name(),
                    null,
                    zero)));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(11L, 12L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    assertNotNull(result);
    OrderDetail detail = result.getDetails().get(0);
    assertEquals(15.0, detail.getUnitPrice().amount().doubleValue(), 0.001);
    assertEquals(0.0, detail.getExtraCharge().amount().doubleValue(), 0.001);
  }

  @Test
  void should_keepExtraChargeAtZero_when_onlyNonExtraCategoriesSelectOptions() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Salad")
            .basePrice(new Money(BigDecimal.valueOf(8.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    OptionCategory dressingCat =
        OptionCategory.builder()
            .id(90L)
            .name("Salsa")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .build();
    OptionCategory withoutOnionCat =
        OptionCategory.builder()
            .id(91L)
            .name("Sin cebolla")
            .selectionType(OptionSelectionType.REMOVE)
            .build();
    ProductOption ranch =
        ProductOption.builder().id(20L).name("Ranch").category(dressingCat).build();
    ProductOption noOnion =
        ProductOption.builder().id(21L).name("Sin Cebolla").category(withoutOnionCat).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(20L, 21L)))
        .thenReturn(List.of(ranch, noOnion));
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 20L)).thenReturn(true);
    when(productOptionRepositoryPort.isOptionAssociatedWithProduct(1L, 21L)).thenReturn(true);
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(20L, 21L))
                        .build()))
            .build();

    Order result = takeOrderUseCase.execute(command);

    OrderDetail detail = result.getDetails().get(0);
    assertEquals(8.0, detail.getUnitPrice().amount().doubleValue(), 0.001);
    assertEquals(0.0, detail.getExtraCharge().amount().doubleValue(), 0.001);
  }
}
