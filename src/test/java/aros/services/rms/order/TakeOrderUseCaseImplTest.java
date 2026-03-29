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
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.application.usecases.TakeOrderCommand;
import aros.services.rms.core.order.application.usecases.TakeOrderUseCaseImpl;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @Mock private InventoryStockUseCase inventoryStockUseCase;

  @Mock private InventoryMovementUseCase inventoryMovementUseCase;

  private TakeOrderUseCaseImpl takeOrderUseCase;

  @BeforeEach
  void setUp() {
    when(inventoryStockUseCase.isAvailable(any(), any())).thenReturn(true);

    takeOrderUseCase =
        new TakeOrderUseCaseImpl(
            orderRepositoryPort,
            tableRepositoryPort,
            productRepositoryPort,
            productOptionRepositoryPort,
            inventoryStockUseCase,
            inventoryMovementUseCase);
  }

  @Test
  void shouldTakeOrderSuccessfully_whenProductHasOptionsAndOptionsProvided() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(10.0)
            .hasOptions(true)
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    ProductOption option =
        ProductOption.builder().id(1L).name("Extra Cheese").product(product).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(1L))).thenReturn(List.of(option));
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
  void shouldTakeOrderSuccessfully_whenProductHasNoOptionsAndNoOptionsProvided() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Water")
            .basePrice(2.0)
            .hasOptions(false)
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
  void shouldThrowAndReleaseTable_whenProductHasOptionsButNoOptionsProvided() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(10.0)
            .hasOptions(true)
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));

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

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> takeOrderUseCase.execute(command));

    assertEquals("Product 'Burger' requires options to be selected", exception.getMessage());
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(tableRepositoryPort, times(2)).save(table);
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowAndReleaseTable_whenProductHasNoOptionsButOptionsProvided() {
    Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
    Product product =
        Product.builder()
            .id(1L)
            .name("Water")
            .basePrice(2.0)
            .hasOptions(false)
            .category(Category.builder().id(1L).name("Drinks").build())
            .build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));

    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(1L)
            .details(
                List.of(
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(1L)
                        .instructions(null)
                        .selectedOptionIds(List.of(1L))
                        .build()))
            .build();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> takeOrderUseCase.execute(command));

    assertEquals("Product 'Water' does not support options", exception.getMessage());
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(tableRepositoryPort, times(2)).save(table);
    verify(orderRepositoryPort, never()).save(any(Order.class));
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
            .basePrice(10.0)
            .hasOptions(true)
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    Product beverage =
        Product.builder()
            .id(2L)
            .name("Beverage")
            .basePrice(5.0)
            .hasOptions(true)
            .category(Category.builder().id(2L).name("Drinks").build())
            .build();

    // Opción válida para beverage, no para burger
    ProductOption invalidOption =
        ProductOption.builder().id(99L).name("Size Large").product(beverage).build();

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
            .basePrice(10.0)
            .hasOptions(true)
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    ProductOption option1 =
        ProductOption.builder().id(1L).name("Extra Cheese").product(product).build();
    ProductOption option2 = ProductOption.builder().id(2L).name("Bacon").product(product).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(productOptionRepositoryPort.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(option1, option2));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

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
}
