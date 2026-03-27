/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Saldo actual de cada variante en cada ubicación. */
@Entity
@Table(
    name = "inventory_stock",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"supply_variant_id", "storage_location_id"})
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supply_variant_id", nullable = false)
  private SupplyVariantEntity supplyVariant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "storage_location_id", nullable = false)
  private StorageLocationEntity storageLocation;

  @Column(name = "current_quantity", nullable = false, precision = 10, scale = 3)
  @Builder.Default
  private BigDecimal currentQuantity = BigDecimal.ZERO;
}
