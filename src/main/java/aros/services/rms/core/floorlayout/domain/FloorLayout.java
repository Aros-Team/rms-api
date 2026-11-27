/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Restaurant-wide schematic floor layout.
 *
 * <p>The layout owns the grid dimensions and the regions painted on it. It is a singleton
 * configuration row, matching the current single-restaurant architecture.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorLayout {
  private int columns;
  private int rows;
  private List<FloorRegion> regions;
  private Instant updatedAt;

  /**
   * Returns the layout used before the first save.
   *
   * @return a 32x20 layout with no painted regions
   */
  public static FloorLayout defaults() {
    return FloorLayout.builder()
        .columns(FloorLayoutLimits.DEFAULT_COLUMNS)
        .rows(FloorLayoutLimits.DEFAULT_ROWS)
        .regions(List.of())
        .build();
  }
}
