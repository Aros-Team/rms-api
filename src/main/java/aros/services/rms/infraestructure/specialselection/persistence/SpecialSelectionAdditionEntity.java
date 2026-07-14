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

/** JPA entity representing a paid addition that can be applied to a special selection. */
@Entity
@Table(name = "special_selection_additions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionAdditionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "option_id", nullable = false)
  private Long optionId;

  @Column(nullable = false)
  private String name;

  @Column(name = "extra_price")
  @Builder.Default
  private Double extraPrice = 0.0;

  @Column(name = "display_order")
  @Builder.Default
  private int displayOrder = 0;
}
