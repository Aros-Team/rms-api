/* (C) 2026 */
package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import aros.services.rms.core.order.application.usecases.UpdateOrderUseCaseImpl;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
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
class UpdateOrderUseCaseImplTest {

  @Mock private OrderRepositoryPort orderRepositoryPort;

  @Mock private TableRepositoryPort tableRepositoryPort;

  @Mock private ProductRepositoryPort productRepositoryPort;

  @Mock private ProductOptionRepositoryPort productOptionRepositoryPort;

  @Mock private InventoryStockUseCase inventoryStockUseCase;

  @Mock private InventoryMovementUseCase inventoryMovementUseCase;

  private UpdateOrderUseCaseImpl updateOrderUseCase;

  @BeforeEach
  void setUp() {
    when(inventoryStockUseCase.isAvailable(any(), any())).thenReturn(true);

    updateOrderUseCase =
        new UpdateOrderUseCaseImpl(
            orderRepositoryPort,
            tableRepositoryPort,
            productRepositoryPort,
            productOptionRepositoryPort,
            inventoryStockUseCase,
            inventoryMovementUseCase);
  }

  // ==================== CANCEL TESTS ====================

  @Test
  void shouldCancelOrderSuccessfully_whenOrderIsInQueueAndHasTable() {
    Table table = Table.builder().id(1L).status(TableStatus.OCCUPIED).build();
    Order order = Order.builder().id(1L).status(OrderStatus.QUEUE).table(table).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(tableRepositoryPort.save(any(Table.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = updateOrderUseCase.cancel(1L);

    assertNotNull(result);
    assertEquals(OrderStatus.CANCELLED, result.getStatus());
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, times(1)).save(order);
    verify(tableRepositoryPort, times(1)).save(table);
  }

  @Test
  void shouldCancelOrderSuccessfully_whenOrderIsInQueueAndHasNoTable() {
    Order order = Order.builder().id(1L).status(OrderStatus.QUEUE).table(null).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = updateOrderUseCase.cancel(1L);

    assertNotNull(result);
    assertEquals(OrderStatus.CANCELLED, result.getStatus());
    assertNull(result.getTable());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, times(1)).save(order);
    verify(tableRepositoryPort, never()).save(any(Table.class));
  }

  @Test
  void shouldThrowException_whenCancelingOrderNotFound() {
    when(orderRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> updateOrderUseCase.cancel(99L));

    assertEquals("Order not found", exception.getMessage());
    verify(orderRepositoryPort, times(1)).findById(99L);
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenCancelingOrderNotInQueue() {
    Order order = Order.builder().id(1L).status(OrderStatus.PREPARING).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> updateOrderUseCase.cancel(1L));

    assertEquals("Order can only be cancelled when in QUEUE status", exception.getMessage());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenCancelingOrderAlreadyDelivered() {
    Order order = Order.builder().id(1L).status(OrderStatus.DELIVERED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> updateOrderUseCase.cancel(1L));

    assertEquals("Order can only be cancelled when in QUEUE status", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenCancelingOrderAlreadyCancelled() {
    Order order = Order.builder().id(1L).status(OrderStatus.CANCELLED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> updateOrderUseCase.cancel(1L));

    assertEquals("Order can only be cancelled when in QUEUE status", exception.getMessage());
  }

  // ==================== UPDATE TESTS ====================

  @Test
  void shouldUpdateOrderSuccessfully_whenProductHasOptionsAndOptionsProvided() {
    Table table = Table.builder().id(1L).status(TableStatus.OCCUPIED).build();
    Order existingOrder = Order.builder().id(1L).status(OrderStatus.QUEUE).table(table).build();

    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(10.0)
            .hasOptions(true)
            .category(Category.builder().id(1L).name("Food").build())
            .build();
    ProductOption option = ProductOption.builder().id(1L).name("Extra Cheese").build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(existingOrder));
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

    Order result = updateOrderUseCase.update(1L, command);

    assertNotNull(result);
    assertEquals(1, result.getDetails().size());
    assertEquals(1, result.getDetails().get(0).getSelectedOptions().size());
    verify(orderRepositoryPort, times(1)).save(existingOrder);
  }

  @Test
  void shouldUpdateOrderSuccessfully_whenProductHasNoOptionsAndNoOptionsProvided() {
    Order existingOrder = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    Product product =
        Product.builder()
            .id(1L)
            .name("Water")
            .basePrice(2.0)
            .hasOptions(false)
            .category(Category.builder().id(1L).name("Drinks").build())
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(existingOrder));
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

    Order result = updateOrderUseCase.update(1L, command);

    assertNotNull(result);
    assertEquals(1, result.getDetails().size());
    assertEquals(0, result.getDetails().get(0).getSelectedOptions().size());
    verify(orderRepositoryPort, times(1)).save(existingOrder);
  }

  @Test
  void shouldThrowException_whenUpdatingOrderNotFound() {
    when(orderRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

    TakeOrderCommand command = TakeOrderCommand.builder().tableId(1L).details(List.of()).build();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> updateOrderUseCase.update(99L, command));

    assertEquals("Order not found", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenUpdatingOrderNotInQueue() {
    Order existingOrder = Order.builder().id(1L).status(OrderStatus.PREPARING).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(existingOrder));

    TakeOrderCommand command = TakeOrderCommand.builder().tableId(1L).details(List.of()).build();

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> updateOrderUseCase.update(1L, command));

    assertEquals("Order can only be updated when in QUEUE status", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenProductHasOptionsButNoOptionsProvided() {
    Order existingOrder = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    Product product =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(10.0)
            .hasOptions(true)
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(existingOrder));
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
        assertThrows(IllegalArgumentException.class, () -> updateOrderUseCase.update(1L, command));

    assertEquals("Product 'Burger' requires options to be selected", exception.getMessage());
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenProductHasNoOptionsButOptionsProvided() {
    Order existingOrder = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    Product product =
        Product.builder()
            .id(1L)
            .name("Water")
            .basePrice(2.0)
            .hasOptions(false)
            .category(Category.builder().id(1L).name("Drinks").build())
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(existingOrder));
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
        assertThrows(IllegalArgumentException.class, () -> updateOrderUseCase.update(1L, command));

    assertEquals("Product 'Water' does not support options", exception.getMessage());
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenProductNotFound() {
    Order existingOrder = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(existingOrder));
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

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> updateOrderUseCase.update(1L, command));

    assertEquals("Product not found", exception.getMessage());
  }

  @Test
  void shouldUpdateOrderWithMultipleDetails() {
    Order existingOrder = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    Product product1 =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(10.0)
            .hasOptions(false)
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    Product product2 =
        Product.builder()
            .id(2L)
            .name("Fries")
            .basePrice(5.0)
            .hasOptions(false)
            .category(Category.builder().id(1L).name("Food").build())
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(existingOrder));
    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product1));
    when(productRepositoryPort.findById(2L)).thenReturn(Optional.of(product2));
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
                        .selectedOptionIds(null)
                        .build(),
                    TakeOrderCommand.OrderDetailCommand.builder()
                        .productId(2L)
                        .instructions("Extra salt")
                        .selectedOptionIds(null)
                        .build()))
            .build();

    Order result = updateOrderUseCase.update(1L, command);

    assertNotNull(result);
    assertEquals(2, result.getDetails().size());
    assertEquals("Burger", result.getDetails().get(0).getProduct().getName());
    assertEquals("Fries", result.getDetails().get(1).getProduct().getName());
  }
}
