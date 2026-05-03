/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence;

import aros.services.rms.infraestructure.category.persistence.OptionCategory;
import jakarta.persistence.Entity;
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

/** Entity representing a product option. */
@Entity
@Table(name = "product_options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne
  @JoinColumn(name = "option_category_id")
  private OptionCategory category;
}
