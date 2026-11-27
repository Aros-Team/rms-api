/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One independent region drawn on the floor layout.
 *
 * <p>A region always points to an existing Area and stores its geometry as compact row segments.
 * The same Area can be represented by several independent regions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorRegion {
  private String key;
  private Long areaId;
  private List<FloorSegment> segments;
}
