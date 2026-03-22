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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Insumo base (ej: "Carne de Res", "Arroz"). */
@Entity
@Table(name = "supplies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supply_category_id", nullable = false)
  private SupplyCategoryEntity category;
}
