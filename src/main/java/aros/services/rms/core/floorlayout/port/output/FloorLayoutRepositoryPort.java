/* (C) 2026 */

package aros.services.rms.core.floorlayout.port.output;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import java.util.List;
import java.util.Optional;

/** Output port for floor layout persistence. */
public interface FloorLayoutRepositoryPort {

  /**
   * Finds the singleton layout.
   *
   * @return the stored layout, if any
   */
  Optional<FloorLayout> find();

  /**
   * Replaces the complete layout and its regions atomically.
   *
   * @param layout validated layout
   * @return the persisted layout with its update timestamp
   */
  FloorLayout save(FloorLayout layout);

  /**
   * Returns every table placement.
   *
   * @return placements ordered by table id
   */
  List<FloorTablePlacement> findTablePlacements();

  /**
   * Replaces every table placement atomically.
   *
   * @param placements complete placement list
   * @return saved placements
   */
  List<FloorTablePlacement> saveTablePlacements(List<FloorTablePlacement> placements);
}
