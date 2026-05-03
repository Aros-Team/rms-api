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

/**
 * Presentación física del insumo (ej: "Carne 250g", "Carne 100g").
 *
 * <p>El nombre NO se repite; se deriva de supply + quantity + unit.
 */
@Entity
@Table(
    name = "supply_variants",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"supply_id", "unit_id", "quantity"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyVariantEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supply_id", nullable = false)
  private SupplyEntity supply;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "unit_id", nullable = false)
  private UnitOfMeasureEntity unit;

  @Column(nullable = false, precision = 10, scale = 3)
  private BigDecimal quantity;
}
