/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence;

import aros.services.rms.infraestructure.product.persistence.ProductOption;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing the join between an {@link OrderDetail} and a selected {@link
 * ProductOption}, persisting the per-option surcharge that was applied at order-taking time (V38).
 *
 * <p>This entity replaces the previous {@code @ManyToMany} between {@code OrderDetail} and {@code
 * ProductOption}. The composite primary key on {@code (order_detail_id, option_id)} matches the PK
 * declared in V1 for the {@code order_detail_options} table — no schema change to the PK/FK
 * semantics.
 *
 * <p>V38 adds {@code extra_price DECIMAL(10,2) NOT NULL DEFAULT 0} so the surcharge of an {@code
 * EXTRA}-category selection can be attributed per option.
 */
@Entity
@Table(name = "order_detail_options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailOption {

  @EmbeddedId private OrderDetailOptionId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("orderDetailId")
  @JoinColumn(name = "order_detail_id")
  private OrderDetail orderDetail;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("optionId")
  @JoinColumn(name = "option_id")
  private ProductOption option;

  /** Per-selection surcharge (V38+). Defaults to 0; populated for EXTRA selections. */
  @Column(name = "extra_price", nullable = false)
  @Builder.Default
  private Double extraPrice = 0.0;

  /** Composite PK for {@link OrderDetailOption}: {@code (order_detail_id, option_id)}. */
  @Embeddable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderDetailOptionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "order_detail_id")
    private Long orderDetailId;

    @Column(name = "option_id")
    private Long optionId;

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof OrderDetailOptionId that)) {
        return false;
      }
      return Objects.equals(orderDetailId, that.orderDetailId)
          && Objects.equals(optionId, that.optionId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(orderDetailId, optionId);
    }
  }
}
