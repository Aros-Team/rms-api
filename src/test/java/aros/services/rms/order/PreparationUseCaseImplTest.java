package aros.services.rms.order;

import aros.services.rms.core.order.application.usecases.PreparationUseCaseImpl;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import java.util.Optional;
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
class PreparationUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    private PreparationUseCaseImpl preparationUseCase;

    @BeforeEach
    void setUp() {
        preparationUseCase = new PreparationUseCaseImpl(orderRepositoryPort);
    }

    @Test
    void shouldProcessNextOrderSuccessfully_whenOrderIsInQueue() {
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.QUEUE)
                .build();

        when(orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE))
                .thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = preparationUseCase.processNextOrder();

        assertNotNull(result);
        assertEquals(OrderStatus.PREPARING, result.getStatus());
        verify(orderRepositoryPort, times(1))
                .findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE);
        verify(orderRepositoryPort, times(1)).save(order);
    }

    @Test
    void shouldThrowException_whenNoOrdersInQueue() {
        when(orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> preparationUseCase.processNextOrder()
        );

        assertEquals("No orders in queue", exception.getMessage());
        verify(orderRepositoryPort, times(1))
                .findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE);
        verify(orderRepositoryPort, times(0)).save(any(Order.class));
    }

    @Test
    void shouldProcessOldestOrderFirst() {
        Order oldestOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.QUEUE)
                .build();

        when(orderRepositoryPort.findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE))
                .thenReturn(Optional.of(oldestOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = preparationUseCase.processNextOrder();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PREPARING, result.getStatus());
    }
}