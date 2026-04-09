/* (C) 2026 */
package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.order.application.service.MarkAsReadyService;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkAsReadyUseCaseImplTest {

  @Mock private OrderRepositoryPort orderRepositoryPort;

  private MarkAsReadyService markAsReadyUseCase;

  @BeforeEach
  void setUp() {
    markAsReadyUseCase = new MarkAsReadyService(orderRepositoryPort);
  }

  @Test
  void shouldMarkOrderAsReadySuccessfully_whenOrderIsPreparing() {
    Order order = Order.builder().id(1L).status(OrderStatus.PREPARING).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = markAsReadyUseCase.markAsReady(1L);

    assertNotNull(result);
    assertEquals(OrderStatus.READY, result.getStatus());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, times(1)).save(order);
  }

  @Test
  void shouldThrowException_whenOrderNotFound() {
    when(orderRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> markAsReadyUseCase.markAsReady(99L));

    assertEquals("Order not found", exception.getMessage());
    verify(orderRepositoryPort, times(1)).findById(99L);
    verify(orderRepositoryPort, times(0)).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenOrderStatusIsQueue() {
    Order order = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> markAsReadyUseCase.markAsReady(1L));

    assertEquals(
        "Order can only be marked as ready when in PREPARING status", exception.getMessage());
    verify(orderRepositoryPort, times(1)).findById(1L);
    verify(orderRepositoryPort, times(0)).save(any(Order.class));
  }

  @Test
  void shouldThrowException_whenOrderStatusIsReady() {
    Order order = Order.builder().id(1L).status(OrderStatus.READY).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> markAsReadyUseCase.markAsReady(1L));

    assertEquals(
        "Order can only be marked as ready when in PREPARING status", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenOrderStatusIsDelivered() {
    Order order = Order.builder().id(1L).status(OrderStatus.DELIVERED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> markAsReadyUseCase.markAsReady(1L));

    assertEquals(
        "Order can only be marked as ready when in PREPARING status", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenOrderStatusIsCancelled() {
    Order order = Order.builder().id(1L).status(OrderStatus.CANCELLED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> markAsReadyUseCase.markAsReady(1L));

    assertEquals(
        "Order can only be marked as ready when in PREPARING status", exception.getMessage());
  }
}
