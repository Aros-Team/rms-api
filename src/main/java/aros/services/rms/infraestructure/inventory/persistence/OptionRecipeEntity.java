/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence;

import aros.services.rms.infraestructure.product.persistence.ProductOption;
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

/** Receta de una opción de producto. */
@Entity
@Table(
    name = "option_recipes",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"option_id", "supply_variant_id"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionRecipeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "option_id", nullable = false)
  private ProductOption option;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supply_variant_id", nullable = false)
  private SupplyVariantEntity supplyVariant;

  @Column(name = "required_quantity", nullable = false, precision = 10, scale = 3)
  private BigDecimal requiredQuantity;
}
