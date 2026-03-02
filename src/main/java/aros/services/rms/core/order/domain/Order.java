package aros.services.rms.core.order.domain;

import aros.services.rms.core.table.domain.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    @Builder.Default
    private LocalDateTime date = LocalDateTime.now();
    private Table table;
    private List<OrderDetail> details;
}