/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Composite key for {@link ProductOptionGroupEntity}. */
@Embeddable
public class ProductOptionGroupId implements Serializable {

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "option_group_id", nullable = false)
  private Long optionGroupId;

  /** Default constructor required by JPA. */
  protected ProductOptionGroupId() {}

  /**
   * Creates a composite key.
   *
   * @param productId the product identifier
   * @param optionGroupId the option group identifier
   */
  public ProductOptionGroupId(Long productId, Long optionGroupId) {
    this.productId = productId;
    this.optionGroupId = optionGroupId;
  }

  public Long getProductId() {
    return productId;
  }

  public Long getOptionGroupId() {
    return optionGroupId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProductOptionGroupId other)) {
      return false;
    }
    return Objects.equals(productId, other.productId)
        && Objects.equals(optionGroupId, other.optionGroupId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, optionGroupId);
  }
}
