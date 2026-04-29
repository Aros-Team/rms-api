/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.persistence;

import aros.services.rms.infraestructure.product.persistence.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity representing the active day menu record. */
@Entity
@Table(name = "day_menu")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMenuEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "valid_from", nullable = false)
  private LocalDateTime validFrom;

  @Column(name = "created_by", nullable = false)
  private String createdBy;
}
