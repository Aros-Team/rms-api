/* (C) 2026 */

package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.order.application.service.DeliveryService;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryUseCaseImplTest {

  @Mock private OrderRepositoryPort orderRepositoryPort;

  @Mock private TableRepositoryPort tableRepositoryPort;

  @Mock private BusinessMetricsPort metricsPort;

  private DeliveryService deliveryUseCase;

  @BeforeEach
  void setUp() {
    deliveryUseCase = new DeliveryService(orderRepositoryPort, tableRepositoryPort, metricsPort);
  }

  @Test
  void shouldMarkOrderAsDeliveredSuccessfully_whenOrderIsReadyAndHasTable() {
    Table table = Table.builder().id(1L).status(TableStatus.OCCUPIED).build();
    Order order = Order.builder().id(1L).status(OrderStatus.READY).table(table).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(tableRepositoryPort.save(any(Table.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = deliveryUseCase.markAsDelivered(1L);

    assertNotNull(result);
    assertEquals(OrderStatus.DELIVERED, result.getStatus());
    assertEquals(TableStatus.AVAILABLE, table.getStatus());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, times(1)).save(order);
    verify(tableRepositoryPort, times(1)).save(table);
  }

  @Test
  void shouldMarkOrderAsDeliveredSuccessfully_whenOrderIsReadyAndHasNoTable() {
    Order order = Order.builder().id(1L).status(OrderStatus.READY).table(null).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = deliveryUseCase.markAsDelivered(1L);

    assertNotNull(result);
    assertEquals(OrderStatus.DELIVERED, result.getStatus());
    assertNull(result.getTable());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, times(1)).save(order);
    verify(tableRepositoryPort, times(0)).save(any(Table.class));
  }

  @Test
  void shouldThrowException_whenOrderNotFound() {
    when(orderRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> deliveryUseCase.markAsDelivered(99L));

    assertEquals("Order not found", exception.getMessage());
    verify(orderRepositoryPort, times(1)).findById(99L);
    verify(orderRepositoryPort, times(0)).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenOrderStatusIsNotReady() {
    Order order = Order.builder().id(1L).status(OrderStatus.PREPARING).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> deliveryUseCase.markAsDelivered(1L));

    assertEquals("Order can only be delivered when in READY status", exception.getMessage());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, times(0)).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenOrderStatusIsQueue() {
    Order order = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> deliveryUseCase.markAsDelivered(1L));

    assertEquals("Order can only be delivered when in READY status", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenOrderStatusIsDelivered() {
    Order order = Order.builder().id(1L).status(OrderStatus.DELIVERED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> deliveryUseCase.markAsDelivered(1L));

    assertEquals("Order can only be delivered when in READY status", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenOrderStatusIsCancelled() {
    Order order = Order.builder().id(1L).status(OrderStatus.CANCELLED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> deliveryUseCase.markAsDelivered(1L));

    assertEquals("Order can only be delivered when in READY status", exception.getMessage());
  }
}
