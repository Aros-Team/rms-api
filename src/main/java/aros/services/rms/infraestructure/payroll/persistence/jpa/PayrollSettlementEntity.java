/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity representing a payroll settlement in the database. */
@Entity
@Table(name = "payroll_settlements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollSettlementEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "payroll_id", nullable = false)
  private Long payrollId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "settlement_type", nullable = false, length = 20)
  private String settlementType;

  @Column(name = "period_start", nullable = false)
  private LocalDate periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDate periodEnd;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(length = 500)
  private String notes;

  @Column(name = "settled_at", nullable = false, updatable = false)
  private Instant settledAt;

  @Column(name = "settled_by", length = 100)
  private String settledBy;

  /** Sets the settlement timestamp before persisting. */
  @PrePersist
  protected void onCreate() {
    settledAt = Instant.now();
  }
}
