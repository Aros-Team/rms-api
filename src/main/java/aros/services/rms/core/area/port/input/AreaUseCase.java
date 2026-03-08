/* (C) 2026 */
package aros.services.rms.core.area.port.input;

import aros.services.rms.core.area.domain.Area;
import java.util.List;

/**
 * Input port for area management operations. Handles creation, update, retrieval, and
 * enable/disable of preparation areas.
 */
public interface AreaUseCase {

  /**
   * Creates a new preparation area.
   *
   * @param area the area to create
   * @return the created area with generated id
   * @throws aros.services.rms.core.area.application.exception.AreaAlreadyExistsException if name
   *     already exists
   */
  Area create(Area area);

  /**
   * Updates an existing preparation area.
   *
   * @param id the area id to update
   * @param area the area data to update
   * @return the updated area
   * @throws aros.services.rms.core.area.application.exception.AreaNotFoundException if area not
   *     found
   */
  Area update(Long id, Area area);

  /**
   * Retrieves all preparation areas.
   *
   * @return list of all areas
   */
  List<Area> findAll();

  /**
   * Retrieves a preparation area by its id.
   *
   * @param id the area id
   * @return the found area
   * @throws aros.services.rms.core.area.application.exception.AreaNotFoundException if area not
   *     found
   */
  Area findById(Long id);

  /**
   * Toggles the enabled status of a preparation area.
   *
   * @param id the area id to toggle
   * @return the updated area with toggled status
   * @throws aros.services.rms.core.area.application.exception.AreaNotFoundException if area not
   *     found
   */
  Area toggleEnabled(Long id);
}