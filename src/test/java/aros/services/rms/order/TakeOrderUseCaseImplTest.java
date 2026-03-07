package aros.services.rms.order;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.order.application.usecases.TakeOrderCommand;
import aros.services.rms.core.order.application.usecases.TakeOrderUseCaseImpl;
import aros.services.rms.core.order.domain.Order;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TakeOrderUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private TableRepositoryPort tableRepositoryPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private ProductOptionRepositoryPort productOptionRepositoryPort;

    private TakeOrderUseCaseImpl takeOrderUseCase;

    @BeforeEach
    void setUp() {
        takeOrderUseCase = new TakeOrderUseCaseImpl(
                orderRepositoryPort,
                tableRepositoryPort,
                productRepositoryPort,
                productOptionRepositoryPort
        );
    }

    @Test
    void shouldTakeOrderSuccessfully_whenProductHasOptionsAndOptionsProvided() {
        Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
        Product product = Product.builder()
                .id(1L)
                .name("Burger")
                .basePrice(10.0)
                .hasOptions(true)
                .category(Category.builder().id(1L).name("Food").build())
                .build();
        ProductOption option = ProductOption.builder().id(1L).name("Extra Cheese").build();

        when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionRepositoryPort.findAllById(List.of(1L))).thenReturn(List.of(option));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(1L)
                .details(List.of(
                        TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(1L)
                                .instructions("No onions")
                                .selectedOptionIds(List.of(1L))
                                .build()
                ))
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
        Product product = Product.builder()
                .id(1L)
                .name("Water")
                .basePrice(2.0)
                .hasOptions(false)
                .category(Category.builder().id(1L).name("Drinks").build())
                .build();

        when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(1L)
                .details(List.of(
                        TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(1L)
                                .instructions(null)
                                .selectedOptionIds(null)
                                .build()
                ))
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
        Product product = Product.builder()
                .id(1L)
                .name("Burger")
                .basePrice(10.0)
                .hasOptions(true)
                .category(Category.builder().id(1L).name("Food").build())
                .build();

        when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));

        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(1L)
                .details(List.of(
                        TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(1L)
                                .instructions(null)
                                .selectedOptionIds(null)
                                .build()
                ))
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> takeOrderUseCase.execute(command)
        );

        assertEquals("Product 'Burger' requires options to be selected", exception.getMessage());
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        verify(tableRepositoryPort, times(2)).save(table);
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowAndReleaseTable_whenProductHasNoOptionsButOptionsProvided() {
        Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();
        Product product = Product.builder()
                .id(1L)
                .name("Water")
                .basePrice(2.0)
                .hasOptions(false)
                .category(Category.builder().id(1L).name("Drinks").build())
                .build();

        when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));

        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(1L)
                .details(List.of(
                        TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(1L)
                                .instructions(null)
                                .selectedOptionIds(List.of(1L))
                                .build()
                ))
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> takeOrderUseCase.execute(command)
        );

        assertEquals("Product 'Water' does not support options", exception.getMessage());
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        verify(tableRepositoryPort, times(2)).save(table);
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowException_whenTableNotFound() {
        when(tableRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(99L)
                .details(List.of())
                .build();

        assertThrows(IllegalArgumentException.class, () -> takeOrderUseCase.execute(command));
    }

    @Test
    void shouldThrowException_whenTableIsNotAvailable() {
        Table table = Table.builder().id(1L).status(TableStatus.OCCUPIED).build();
        when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));

        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(1L)
                .details(List.of())
                .build();

        assertThrows(IllegalStateException.class, () -> takeOrderUseCase.execute(command));
    }

    @Test
    void shouldThrowAndReleaseTable_whenProductNotFound() {
        Table table = Table.builder().id(1L).status(TableStatus.AVAILABLE).build();

        when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));
        when(productRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(1L)
                .details(List.of(
                        TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(99L)
                                .instructions(null)
                                .selectedOptionIds(null)
                                .build()
                ))
                .build();

        assertThrows(IllegalArgumentException.class, () -> takeOrderUseCase.execute(command));
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        verify(tableRepositoryPort, times(2)).save(table);
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }
}
