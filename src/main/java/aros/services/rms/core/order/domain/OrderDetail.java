/* (C) 2026 */

package aros.services.rms.core.order.domain;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single line item within an order, including the product, its selected options and
 * any clarifications or additions captured when the order was taken.
 *
 * <p>Phase C — orders: carries {@code extraCharge} (sum of {@code EXTRA}-category surcharges) and a
 * per-option surcharge map used by the persistence layer when creating {@code order_detail_options}
 * rows. The per-option map defaults to empty; populating it is the responsibility of {@link
 * aros.services.rms.core.order.application.service.TakeOrderService}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
  private Long id;
  private Product product;
  @Builder.Default private Money unitPrice = Money.zero(Currency.getInstance("COP"));

  /**
   * Surcharge attributed to {@code EXTRA}-category option selections. Persisted on the entity via
   * the {@code order_detail_options.extra_price} column (summed across rows at toDomain time).
   */
  @Builder.Default private Money extraCharge = Money.zero(Currency.getInstance("COP"));

  /**
   * Per-option surcharge map (option id → surcharge). Populated by the order-taking service so the
   * persistence layer can write the per-row {@code extra_price} value when creating the join rows.
   * Defaults to empty; the persistence layer treats {@code null} or empty entries as 0.
   */
  @Builder.Default private Map<Long, Money> optionExtraPrices = new HashMap<>();

  private String instructions;
  private List<ProductOption> selectedOptions;
  private List<Long> selectedProductIds;
  private List<Long> additionIds;
  private List<ClarificationAnswer> clarifications;
}
