/* (C) 2026 */

package aros.services.rms.core.area.port.output;

import aros.services.rms.core.area.domain.Area;
import java.util.List;
import java.util.Optional;

/** Output port for area persistence operations. */
public interface AreaRepositoryPort {
  /**
   * Saves an area to the repository.
   *
   * @param area the area to save
   * @return the saved area with generated ID
   */
  Area save(Area area);

  /**
   * Finds an area by its identifier.
   *
   * @param id the area identifier
   * @return Optional containing the area if found
   */
  Optional<Area> findById(Long id);

  /**
   * Finds an area by its name.
   *
   * @param name the area name to search
   * @return Optional containing the area if found
   */
  Optional<Area> findByName(String name);

  /**
   * Retrieves all areas.
   *
   * @return list of all areas
   */
  List<Area> findAll();

  /**
   * Deletes an area by its identifier.
   *
   * @param id the area identifier to delete
   */
  void deleteById(Long id);

  /**
   * Checks if an area exists by its identifier.
   *
   * @param id the area identifier to check
   * @return true if area exists
   */
  boolean existsById(Long id);

  /**
   * Finds multiple areas by their identifiers.
   *
   * @param ids list of area identifiers to find
   * @return list of found areas
   */
  List<Area> findByIdIn(List<Long> ids);
}
