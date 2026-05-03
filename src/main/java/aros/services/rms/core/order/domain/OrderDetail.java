/* (C) 2026 */

package aros.services.rms.core.order.domain;

import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a single item in an order with its selected options and special
 * instructions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
  private Long id;
  private Product product;
  private Double unitPrice;
  private String instructions;
  private List<ProductOption> selectedOptions;
}
