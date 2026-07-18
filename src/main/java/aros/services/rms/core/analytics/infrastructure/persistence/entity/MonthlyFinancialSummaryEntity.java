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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** JPA entity for the monthly_financial_summary table. */
@Entity
@Table(name = "monthly_financial_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFinancialSummaryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "period_key", nullable = false, length = 10)
  private String periodKey;

  @Column(name = "bucket", nullable = false, length = 10)
  private String bucket;

  @Column(name = "net_sales", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal netSales = BigDecimal.ZERO;

  @Column(name = "gross_sales", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal grossSales = BigDecimal.ZERO;

  @Column(nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal discounts = BigDecimal.ZERO;

  @Column(nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal comped = BigDecimal.ZERO;

  @Column(name = "cogs_food", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal cogsFood = BigDecimal.ZERO;

  @Column(name = "cogs_beverage", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal cogsBeverage = BigDecimal.ZERO;

  @Column(name = "cogs_alcohol", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal cogsAlcohol = BigDecimal.ZERO;

  @Column(name = "cogs_other", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal cogsOther = BigDecimal.ZERO;

  @Column(name = "food_cogs_pct", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal foodCogsPct = BigDecimal.ZERO;

  @Column(name = "labor_foh", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal laborFoh = BigDecimal.ZERO;

  @Column(name = "labor_boh", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal laborBoh = BigDecimal.ZERO;

  @Column(name = "labor_total", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal laborTotal = BigDecimal.ZERO;

  @Column(name = "labor_pct", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal laborPct = BigDecimal.ZERO;

  @Column(name = "prime_cost", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal primeCost = BigDecimal.ZERO;

  @Column(name = "prime_cost_pct", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal primeCostPct = BigDecimal.ZERO;

  @Column(name = "gross_profit_pct", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal grossProfitPct = BigDecimal.ZERO;

  @Column(name = "net_profit_pct", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal netProfitPct = BigDecimal.ZERO;

  @Column(name = "data_completeness", nullable = false, length = 10)
  @Builder.Default
  private String dataCompleteness = "FULL";

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
