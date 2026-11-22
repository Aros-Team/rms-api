/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.entity;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity for the system_alerts table. */
@Entity
@Table(name = "system_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemAlertEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private AlertSeverity severity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private AlertType type;

  @Column(nullable = false, length = 100)
  private String metric;

  @Column(precision = 14, scale = 4)
  private BigDecimal baseline;

  @Column(precision = 14, scale = 4)
  private BigDecimal current;

  @Column(name = "deviation_pct", precision = 10, scale = 2)
  private BigDecimal deviationPct;

  @Column(name = "period_key", nullable = false, length = 10)
  private String periodKey;

  @Column(name = "period_bucket", nullable = false, length = 10)
  private String periodBucket;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(name = "detected_at", nullable = false)
  private Instant detectedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AlertStatus status;

  @Column(name = "read_at")
  private Instant readAt;
}
