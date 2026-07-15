/* (C) 2026 */

package aros.services.rms.core.purchase.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model for a single line item within a purchase order. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItem {

  private Long id;
  private Long purchaseOrderId;
  private Long supplyVariantId;
  private BigDecimal quantityOrdered;
  private BigDecimal quantityReceived;
  @Builder.Default private Money unitPrice = Money.zero(Currency.getInstance("COP"));
}
