/* (C) 2026 */

package aros.services.rms.core.daymenu.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model representing the active day menu configuration. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMenu {
  private Long id;
  private Long productId;
  private String productName;
  private Double productBasePrice;
  private LocalDateTime validFrom;
  private String createdBy;
}
