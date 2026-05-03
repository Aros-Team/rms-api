/* (C) 2026 */

package aros.services.rms.core.purchase.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
  private BigDecimal totalAmount;
  private String notes;
  private LocalDateTime createdAt;
  private List<PurchaseOrderItem> items;
}
