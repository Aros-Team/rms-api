package aros.services.rms.order;

import aros.services.rms.core.order.application.usecases.OrderQueryUseCaseImpl;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    private OrderQueryUseCaseImpl orderQueryUseCase;

    @BeforeEach
    void setUp() {
        orderQueryUseCase = new OrderQueryUseCaseImpl(orderRepositoryPort);
    }

    @Test
    void shouldFindAllOrders_whenNoFiltersProvided() {
        List<Order> orders = List.of(
                Order.builder().id(1L).status(OrderStatus.QUEUE).build(),
                Order.builder().id(2L).status(OrderStatus.PREPARING).build()
        );

        when(orderRepositoryPort.findAll()).thenReturn(orders);

        List<Order> result = orderQueryUseCase.findOrders(null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepositoryPort, times(1)).findAll();
        verify(orderRepositoryPort, times(0)).findByStatus(any());
        verify(orderRepositoryPort, times(0)).findByDateBetween(any(), any());
        verify(orderRepositoryPort, times(0)).findByStatusAndDateBetween(any(), any(), any());
    }

    @Test
    void shouldFindOrdersByStatus_whenOnlyStatusProvided() {
        List<Order> orders = List.of(
                Order.builder().id(1L).status(OrderStatus.QUEUE).build(),
                Order.builder().id(2L).status(OrderStatus.QUEUE).build()
        );

        when(orderRepositoryPort.findByStatus(OrderStatus.QUEUE)).thenReturn(orders);

        List<Order> result = orderQueryUseCase.findOrders(OrderStatus.QUEUE, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(OrderStatus.QUEUE, result.get(0).getStatus());
        verify(orderRepositoryPort, times(1)).findByStatus(OrderStatus.QUEUE);
        verify(orderRepositoryPort, times(0)).findAll();
    }

    @Test
    void shouldFindOrdersByDateRange_whenOnlyDatesProvided() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Order> orders = List.of(
                Order.builder().id(1L).build(),
                Order.builder().id(2L).build()
        );

        when(orderRepositoryPort.findByDateBetween(startDate, endDate)).thenReturn(orders);

        List<Order> result = orderQueryUseCase.findOrders(null, startDate, endDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepositoryPort, times(1)).findByDateBetween(startDate, endDate);
        verify(orderRepositoryPort, times(0)).findAll();
    }

    @Test
    void shouldFindOrdersByStatusAndDateRange_whenAllFiltersProvided() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Order> orders = List.of(
                Order.builder().id(1L).status(OrderStatus.DELIVERED).build()
        );

        when(orderRepositoryPort.findByStatusAndDateBetween(OrderStatus.DELIVERED, startDate, endDate))
                .thenReturn(orders);

        List<Order> result = orderQueryUseCase.findOrders(OrderStatus.DELIVERED, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepositoryPort, times(1))
                .findByStatusAndDateBetween(OrderStatus.DELIVERED, startDate, endDate);
        verify(orderRepositoryPort, times(0)).findAll();
    }

    @Test
    void shouldThrowException_whenEndDateIsInFuture() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderQueryUseCase.findOrders(null, startDate, endDate)
        );

        assertEquals("End date cannot be in the future", exception.getMessage());
        verify(orderRepositoryPort, times(0)).findByDateBetween(any(), any());
    }

    @Test
    void shouldThrowException_whenStartDateIsAfterEndDate() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderQueryUseCase.findOrders(null, startDate, endDate)
        );

        assertEquals("Start date cannot be after end date", exception.getMessage());
        verify(orderRepositoryPort, times(0)).findByDateBetween(any(), any());
    }

    @Test
    void shouldFindOrders_whenOnlyStartDateIsNull() {
        LocalDateTime endDate = LocalDateTime.now();
        List<Order> orders = List.of(Order.builder().id(1L).build());

        when(orderRepositoryPort.findAll()).thenReturn(orders);

        List<Order> result = orderQueryUseCase.findOrders(null, null, endDate);

        assertNotNull(result);
        verify(orderRepositoryPort, times(1)).findAll();
    }

    @Test
    void shouldFindOrders_whenOnlyEndDateIsNull() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<Order> orders = List.of(Order.builder().id(1L).build());

        when(orderRepositoryPort.findAll()).thenReturn(orders);

        List<Order> result = orderQueryUseCase.findOrders(null, startDate, null);

        assertNotNull(result);
        verify(orderRepositoryPort, times(1)).findAll();
    }
}