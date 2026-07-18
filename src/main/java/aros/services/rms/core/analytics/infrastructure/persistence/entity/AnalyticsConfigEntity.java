/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** JPA entity for the singleton analytics configuration row. */
@Entity
@Table(name = "analytics_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsConfigEntity {

  @Id private Integer id;

  @Column(name = "default_open", nullable = false)
  private LocalTime defaultOpen;

  @Column(name = "default_close", nullable = false)
  private LocalTime defaultClose;

  @Column(name = "lunch_start", nullable = false)
  private LocalTime lunchStart;

  @Column(name = "lunch_end", nullable = false)
  private LocalTime lunchEnd;

  @Column(name = "dinner_start", nullable = false)
  private LocalTime dinnerStart;

  @Column(name = "dinner_end", nullable = false)
  private LocalTime dinnerEnd;

  @Column(name = "food_cost_deviation_pp", precision = 5, scale = 2, nullable = false)
  private BigDecimal foodCostDeviationPp;

  @Column(name = "labor_cost_deviation_pp", precision = 5, scale = 2, nullable = false)
  private BigDecimal laborCostDeviationPp;

  @Column(name = "sales_drop_yoy_pct", precision = 5, scale = 2, nullable = false)
  private BigDecimal salesDropYoyPct;

  @CreationTimestamp
  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt;

  @Column(name = "updated_by")
  private Long updatedBy;
}
