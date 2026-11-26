/* (C) 2026 */

package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.order.application.exception.OrderDetailNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.service.MarkAsReadyService;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link MarkAsReadyUseCase}.
 *
 * <p><b>Feature:</b> Order preparation completion
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>set Up
 *   <li>should Mark Order As Ready Successfully when Order Is Preparing
 *   <li>should Throw Exception when Order Not Found
 *   <li>should Throw Exception when Order Status Is Queue
 *   <li>should Throw Exception when Order Status Is Ready
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks BusinessMetricsPort, OrderRepositoryPort dependencies via Mockito
 *   <li>Verifies business logic with unit-level assertions
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class MarkAsReadyUseCaseImplTest {

  @Mock private OrderRepositoryPort orderRepositoryPort;

  @Mock private BusinessMetricsPort metricsPort;

  private MarkAsReadyService markAsReadyUseCase;

  @BeforeEach
  void setUp() {
    markAsReadyUseCase = new MarkAsReadyService(orderRepositoryPort, metricsPort);
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

    assertEquals("Pedido no encontrado", exception.getMessage());
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
        "El pedido solo se puede marcar como listo cuando está en estado PREPARING",
        exception.getMessage());
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
        "El pedido solo se puede marcar como listo cuando está en estado PREPARING",
        exception.getMessage());
  }

  @Test
  void shouldThrowException_whenOrderStatusIsDelivered() {
    Order order = Order.builder().id(1L).status(OrderStatus.DELIVERED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> markAsReadyUseCase.markAsReady(1L));

    assertEquals(
        "El pedido solo se puede marcar como listo cuando está en estado PREPARING",
        exception.getMessage());
  }

  @Test
  void shouldThrowException_whenOrderStatusIsCancelled() {
    Order order = Order.builder().id(1L).status(OrderStatus.CANCELLED).build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> markAsReadyUseCase.markAsReady(1L));

    assertEquals(
        "El pedido solo se puede marcar como listo cuando está en estado PREPARING",
        exception.getMessage());
  }

  // ---------------------------------------------------------------------------
  // item-level readiness
  // ---------------------------------------------------------------------------

  private static OrderDetail detail(long id, boolean ready) {
    return OrderDetail.builder().id(id).ready(ready).build();
  }

  @Test
  void shouldMarkDetailReady_andKeepOrderPreparing_whenOtherDetailsPending() {
    Order order =
        Order.builder()
            .id(1L)
            .status(OrderStatus.PREPARING)
            .details(new ArrayList<>(List.of(detail(10L, false), detail(11L, false))))
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = markAsReadyUseCase.markDetailAsReady(1L, 10L);

    assertEquals(OrderStatus.PREPARING, result.getStatus());
    assertTrue(result.getDetails().get(0).isReady());
    assertNotNull(result.getDetails().get(0).getReadyAt());
    assertFalse(result.getDetails().get(1).isReady());
    assertNull(result.getDetails().get(1).getReadyAt());
    verify(orderRepositoryPort, times(1)).save(order);
    verifyNoInteractions(metricsPort);
  }

  @Test
  void shouldMarkOrderReady_whenLastDetailBecomesReady() {
    Order order =
        Order.builder()
            .id(1L)
            .status(OrderStatus.PREPARING)
            .date(java.time.LocalDateTime.now())
            .details(new ArrayList<>(List.of(detail(10L, true), detail(11L, false))))
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = markAsReadyUseCase.markDetailAsReady(1L, 11L);

    assertEquals(OrderStatus.READY, result.getStatus());
    assertTrue(result.getDetails().stream().allMatch(OrderDetail::isReady));
    verify(metricsPort, times(1)).recordOrderStatusTransition("PREPARING", "READY");
  }

  @Test
  void shouldBeIdempotent_whenDetailIsAlreadyReady() {
    Order order =
        Order.builder()
            .id(1L)
            .status(OrderStatus.PREPARING)
            .details(new ArrayList<>(List.of(detail(10L, true), detail(11L, false))))
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    Order result = markAsReadyUseCase.markDetailAsReady(1L, 10L);

    assertEquals(OrderStatus.PREPARING, result.getStatus());
    verify(orderRepositoryPort, never()).save(any(Order.class));
    verifyNoInteractions(metricsPort);
  }

  @Test
  void shouldThrowOrderNotFound_whenMarkingDetailOfUnknownOrder() {
    when(orderRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    OrderNotFoundException exception =
        assertThrows(
            OrderNotFoundException.class, () -> markAsReadyUseCase.markDetailAsReady(99L, 1L));

    assertEquals("Pedido no encontrado: 99", exception.getMessage());
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowDetailNotFound_whenDetailDoesNotBelongToOrder() {
    Order order =
        Order.builder()
            .id(1L)
            .status(OrderStatus.PREPARING)
            .details(new ArrayList<>(List.of(detail(10L, false))))
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    OrderDetailNotFoundException exception =
        assertThrows(
            OrderDetailNotFoundException.class,
            () -> markAsReadyUseCase.markDetailAsReady(1L, 404L));

    assertEquals("Detalle del pedido no encontrado: 404 en el pedido 1", exception.getMessage());
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldRejectDetailReadiness_whenOrderIsQueue() {
    Order order =
        Order.builder()
            .id(1L)
            .status(OrderStatus.QUEUE)
            .details(new ArrayList<>(List.of(detail(10L, false))))
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> markAsReadyUseCase.markDetailAsReady(1L, 10L));

    assertEquals(
        "Los detalles del pedido solo se pueden marcar como listos cuando están "
            + "en estado PREPARING",
        exception.getMessage());
    verify(orderRepositoryPort, never()).save(any(Order.class));
  }

  @Test
  void shouldMarkEveryDetailReady_whenOrderLevelReady() {
    Order order =
        Order.builder()
            .id(1L)
            .status(OrderStatus.PREPARING)
            .details(new ArrayList<>(List.of(detail(10L, false), detail(11L, false))))
            .build();

    when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = markAsReadyUseCase.markAsReady(1L);

    assertEquals(OrderStatus.READY, result.getStatus());
    assertTrue(result.getDetails().stream().allMatch(OrderDetail::isReady));
    assertTrue(result.getDetails().stream().allMatch(d -> d.getReadyAt() != null));
  }
}
