/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One table placed on the restaurant floor grid. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorTablePlacement {
  private Long tableId;
  private int row;
  private int column;
}
