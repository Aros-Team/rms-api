package aros.services.rms.core.order.domain;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import java.util.Currency;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single line item within an order, including the product, its selected options and
 * any clarifications or additions captured when the order was taken.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
  private Long id;
  private Product product;
  @Builder.Default private Money unitPrice = Money.zero(Currency.getInstance("COP"));
  private String instructions;
  private List<ProductOption> selectedOptions;
  private List<Long> selectedProductIds;
  private List<Long> additionIds;
  private List<ClarificationAnswer> clarifications;
}
