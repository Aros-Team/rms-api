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
 * JPA entity representing the association between a special selection group and one of its
 * products.
 */
@Entity
@Table(name = "special_selection_group_products")
@IdClass(GroupProductEntity.GroupProductId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupProductEntity {

  @Id
  @Column(name = "group_id")
  private Long groupId;

  @Id
  @Column(name = "product_id")
  private Long productId;

  /** Composite primary key for group-product associations. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupProductId implements Serializable {
    private Long groupId;
    private Long productId;
  }
}
