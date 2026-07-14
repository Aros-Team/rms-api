package aros.services.rms.infraestructure.specialselection.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity representing a product-category group within a special selection configuration. */
@Entity
@Table(name = "special_selection_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionGroupEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "category_id")
  private Long categoryId;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private int displayOrder = 0;

  @Column(nullable = false)
  @Builder.Default
  private boolean required = false;

  @Column(name = "min_selections", nullable = false)
  @Builder.Default
  private int minSelections = 1;

  @Column(name = "max_selections", nullable = false)
  @Builder.Default
  private int maxSelections = 1;
}
