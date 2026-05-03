/* (C) 2026 */

package aros.services.rms.core.order.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command object for creating a new order. Contains the table ID and list of product details to
 * order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakeOrderCommand {
  private Long tableId;
  private List<OrderDetailCommand> details;

  /**
   * Command object for a single product in the order with its selected options and special
   * instructions.
   */
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
