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

/** Entity representing a payroll event in the database. */
@Entity
@Table(name = "payroll_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "event_date", nullable = false)
  private LocalDate eventDate;

  @Column(name = "event_type", nullable = false, length = 30)
  private String eventType;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal quantity;

  @Column(name = "unit_rate", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitRate;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(length = 500)
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 100)
  private String createdBy;

  /** Sets the creation timestamp before persisting. */
  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }
}
