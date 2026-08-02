/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity for the {@code product_option_groups} junction table (V40).
 *
 * <p>Models the high-level M:N relationship between a Product and the OptionGroups that apply to
 * it. The {@code required} flag indicates whether the option group is mandatory at order-taking
 * time (e.g. "Tipo de proteína" must always be chosen for a hamburger).
 *
 * <p>The per-option detail (extra_price, display_order) lives in {@code product_product_options}
 * (V25); this table only models which groups a product exposes.
 */
@Entity
@Table(name = "product_option_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionGroupEntity {

  @EmbeddedId private ProductOptionGroupId id;

  @Column(name = "required", nullable = false)
  private boolean required;
}
