/* (C) 2026 */

package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.service.PreparationService;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link PreparationUseCase}.
 *
 * <p><b>Feature:</b> Order preparation workflow
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>set Up
 *   <li>should Process Next Order Successfully when Order Is In Queue
 *   <li>should Throw Exception when No Orders In Queue
 *   <li>should Process Oldest Order First
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
class PreparationUseCaseImplTest {

  @Mock private OrderRepositoryPort orderRepositoryPort;

  @Mock private BusinessMetricsPort metricsPort;

  private PreparationService preparationUseCase;

  @BeforeEach
  void setUp() {
    preparationUseCase = new PreparationService(orderRepositoryPort, metricsPort);
  }

  @Test
  void shouldProcessNextOrderSuccessfully_whenOrderIsInQueue() {
    Order order = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    when(orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE))
        .thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = preparationUseCase.processNextOrder();

    assertNotNull(result);
    assertEquals(OrderStatus.PREPARING, result.getStatus());
    verify(orderRepositoryPort, times(1)).findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE);
    verify(orderRepositoryPort, times(1)).save(order);
  }

  @Test
  void shouldThrowException_whenNoOrdersInQueue() {
    when(orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE))
        .thenReturn(Optional.empty());

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> preparationUseCase.processNextOrder());

    assertEquals("No hay pedidos en cola", exception.getMessage());
    verify(orderRepositoryPort, times(1)).findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE);
    verify(orderRepositoryPort, times(0)).save(any(Order.class));
  }

  @Test
  void shouldProcessOldestOrderFirst() {
    Order oldestOrder = Order.builder().id(1L).status(OrderStatus.QUEUE).build();

    when(orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE))
        .thenReturn(Optional.of(oldestOrder));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = preparationUseCase.processNextOrder();

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(OrderStatus.PREPARING, result.getStatus());
  }

  @Test
  void shouldPrepareSpecificOrder_whenOrderIsInQueue() {
    Order order = Order.builder().id(5L).status(OrderStatus.QUEUE).build();

    when(orderRepositoryPort.findById(5L)).thenReturn(Optional.of(order));
    when(orderRepositoryPort.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = preparationUseCase.prepareOrder(5L);

    assertNotNull(result);
    assertEquals(5L, result.getId());
    assertEquals(OrderStatus.PREPARING, result.getStatus());
    verify(orderRepositoryPort, times(1)).findById(5L);
    verify(orderRepositoryPort, never()).findFirstByStatusOrderByDateAsc(any());
  }

  @Test
  void shouldThrowOrderNotFound_whenPreparingUnknownOrder() {
    when(orderRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    OrderNotFoundException exception =
        assertThrows(OrderNotFoundException.class, () -> preparationUseCase.prepareOrder(99L));

    assertEquals("Pedido no encontrado: 99", exception.getMessage());
    verify(orderRepositoryPort, times(0)).save(any(Order.class));
  }

  @Test
  void shouldRejectPreparation_whenOrderIsNotInQueue() {
    Order order = Order.builder().id(6L).status(OrderStatus.PREPARING).build();

    when(orderRepositoryPort.findById(6L)).thenReturn(Optional.of(order));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> preparationUseCase.prepareOrder(6L));

    assertEquals(
        "El pedido solo se puede preparar cuando está en estado QUEUE", exception.getMessage());
    verify(orderRepositoryPort, times(0)).save(any(Order.class));
  }
}
