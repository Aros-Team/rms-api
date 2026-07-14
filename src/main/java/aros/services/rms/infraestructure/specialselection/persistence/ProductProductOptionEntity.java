package aros.services.rms.infraestructure.specialselection.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing the association between a product and one of its product options within a
 * special selection configuration.
 */
@Entity
@Table(name = "product_product_options")
@IdClass(ProductProductOptionEntity.ProductProductOptionId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductProductOptionEntity {

  @Id
  @Column(name = "product_id")
  private Long productId;

  @Id
  @Column(name = "option_id")
  private Long optionId;

  @Column(name = "selection_group_id")
  private Long selectionGroupId;

  @Column(name = "extra_price")
  @Builder.Default
  private Double extraPrice = 0.0;

  @Column(name = "display_order")
  @Builder.Default
  private int displayOrder = 0;

  /** Composite identifier for the product-product-option association. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductProductOptionId implements Serializable {
    private Long productId;
    private Long optionId;
  }
}
