/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence;

import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.infraestructure.order.persistence.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Historial de movimientos de inventario (traslados y deducciones). */
@Entity
@Table(name = "inventory_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supply_variant_id", nullable = false)
  private SupplyVariantEntity supplyVariant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_storage_location_id")
  private StorageLocationEntity fromStorageLocation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_storage_location_id")
  private StorageLocationEntity toStorageLocation;

  @Column(nullable = false, precision = 10, scale = 3)
  private BigDecimal quantity;

  @Enumerated(EnumType.STRING)
  @Column(name = "movement_type", nullable = false, length = 50)
  private MovementType movementType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reference_order_id")
  private Order referenceOrder;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
