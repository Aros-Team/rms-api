/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence;

import aros.services.rms.infraestructure.product.persistence.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an order detail.
 *
 * <p>Selected options are persisted via the {@link OrderDetailOption} join entity (Phase C), which
 * carries the per-selection surcharge ({@code extra_price}, V38). The previous {@code @ManyToMany}
 * between {@code OrderDetail} and {@code ProductOption} was replaced by this one-to-many because
 * the join table now carries attribute data.
 */
@Entity
@Table(name = "order_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  private Double unitPrice;

  private String instructions;

  /**
   * Selected option rows. Each row pairs an {@link
   * aros.services.rms.infraestructure.product.persistence.ProductOption} with its per-selection
   * surcharge. Backed by the {@code order_detail_options} join table.
   */
  @OneToMany(
      mappedBy = "orderDetail",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<OrderDetailOption> selectedOptions = new ArrayList<>();
}
