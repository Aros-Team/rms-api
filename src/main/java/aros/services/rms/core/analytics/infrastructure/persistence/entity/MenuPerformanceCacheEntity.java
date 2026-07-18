/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

/** JPA entity for the menu_performance_cache table. */
@Entity
@Table(name = "menu_performance_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuPerformanceCacheEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "category_id")
  private Long categoryId;

  @Column(name = "category_name", length = 255)
  private String categoryName;

  @Column(name = "period_key", nullable = false, length = 10)
  private String periodKey;

  @Column(nullable = false, length = 10)
  private String bucket;

  @Column(name = "units_sold", nullable = false)
  @Builder.Default
  private int unitsSold = 0;

  @Column(nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal revenue = BigDecimal.ZERO;

  @Column(name = "recipe_cost", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal recipeCost = BigDecimal.ZERO;

  @Column(name = "gross_profit_per_unit", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal grossProfitPerUnit = BigDecimal.ZERO;

  @Column(name = "total_contribution", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal totalContribution = BigDecimal.ZERO;

  @Column(nullable = false, length = 20)
  private String quadrant;

  @Column(name = "source_version", nullable = false, length = 20)
  private String sourceVersion;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
