package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public class TakeOrderUseCaseImpl implements TakeOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final TableRepositoryPort tableRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductOptionRepositoryPort productOptionRepositoryPort;

    public TakeOrderUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort,
            TableRepositoryPort tableRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            ProductOptionRepositoryPort productOptionRepositoryPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.tableRepositoryPort = tableRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.productOptionRepositoryPort = productOptionRepositoryPort;
    }

    public Order execute(TakeOrderCommand command) {
        Table table = tableRepositoryPort.findById(command.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException("Table is not available");
        }

        table.setStatus(TableStatus.OCCUPIED);
        tableRepositoryPort.save(table);

        Order order = Order.builder()
                .table(table)
                .date(LocalDateTime.now())
                .details(new ArrayList<>())
                .build();

        for (TakeOrderCommand.OrderDetailCommand detailCommand : command.getDetails()) {
            Product product = productRepositoryPort.findById(detailCommand.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            List<ProductOption> selectedOptions = new ArrayList<>();
            if (detailCommand.getSelectedOptionIds() != null && !detailCommand.getSelectedOptionIds().isEmpty()) {
                selectedOptions = productOptionRepositoryPort.findAllById(detailCommand.getSelectedOptionIds());
            }

            OrderDetail detail = OrderDetail.builder()
                    .product(product)
                    .unitPrice(product.getBasePrice())
                    .instructions(detailCommand.getInstructions())
                    .selectedOptions(selectedOptions)
                    .build();

            order.getDetails().add(detail);
        }

        return orderRepositoryPort.save(order);
    }
}