/* (C) 2026 */
package aros.services.rms.core.daymenu.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model representing an archived day menu entry. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMenuHistory {
  private Long id;
  private Long productId;
  private String productName;
  private Double productBasePrice;
  private LocalDateTime validFrom;
  private LocalDateTime validUntil;
  private String createdBy;
}
