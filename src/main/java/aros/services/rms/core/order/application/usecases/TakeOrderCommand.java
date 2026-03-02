package aros.services.rms.core.order.application.usecases;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakeOrderCommand {
    private Long tableId;
    private List<OrderDetailCommand> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailCommand {
        private Long productId;
        private String instructions;
        private List<Long> selectedOptionIds;
    }
}