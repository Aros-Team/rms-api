/* (C) 2026 */

package aros.services.rms.core.floorlayout.port.input;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import java.util.List;

/** Input port for reading and replacing the restaurant floor layout. */
public interface FloorLayoutUseCase {

  /**
   * Returns the stored layout or the documented defaults before the first save.
   *
   * @return current floor layout
   */
  FloorLayout get();

  /**
   * Validates and atomically replaces the complete floor layout.
   *
   * @param layout complete submitted layout
   * @return canonical persisted layout
   */
  FloorLayout save(FloorLayout layout);

  /**
   * Returns the current table placements.
   *
   * @return table placements
   */
  List<FloorTablePlacement> getTablePlacements();

  /**
   * Validates and atomically replaces every table placement.
   *
   * @param placements complete placement list
   * @return saved placements
   */
  List<FloorTablePlacement> saveTablePlacements(List<FloorTablePlacement> placements);
}
