/* (C) 2026 */

package aros.services.rms.core.order.domain;

import aros.services.rms.core.table.domain.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a restaurant order. Tracks the order lifecycle from QUEUE through
 * PREPARING and READY to DELIVERED.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
  private Long id;
  @Builder.Default private LocalDateTime date = LocalDateTime.now();
  @Builder.Default private OrderStatus status = OrderStatus.QUEUE;
  private Table table;
  private List<OrderDetail> details;
  @Builder.Default private Set<Long> preparationAreaIds = new HashSet<>();
}
