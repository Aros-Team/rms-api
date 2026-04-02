/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.persistence;

import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity for a single line item within a purchase order. */
@Entity
@Table(name = "purchase_order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_order_id", nullable = false)
  private PurchaseOrderEntity purchaseOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supply_variant_id", nullable = false)
  private SupplyVariantEntity supplyVariant;

  @Column(name = "quantity_ordered", nullable = false, precision = 10, scale = 3)
  private BigDecimal quantityOrdered;

  @Column(name = "quantity_received", nullable = false, precision = 10, scale = 3)
  private BigDecimal quantityReceived;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;
}
