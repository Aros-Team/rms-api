/* (C) 2026 */

package aros.services.rms.core.purchase.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model for a purchase order (cabecera de compra). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

  private Long id;
  private Long supplierId;
  private Long registeredById;
  private LocalDateTime purchasedAt;
  @Builder.Default private Money totalAmount = Money.zero(Currency.getInstance("COP"));
  private String notes;
  private LocalDateTime createdAt;
  private List<PurchaseOrderItem> items;
}
