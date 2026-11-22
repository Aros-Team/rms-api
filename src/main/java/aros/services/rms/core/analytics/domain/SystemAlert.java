/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A persisted variance alert. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemAlert {

  private Long id;
  private AlertSeverity severity;
  private AlertType type;
  private String metric;
  private BigDecimal baseline;
  private BigDecimal current;
  private BigDecimal deviationPct;
  private String periodKey;
  private String periodBucket;
  private String message;
  private Instant detectedAt;
  private AlertStatus status;
  private Instant readAt;
}
