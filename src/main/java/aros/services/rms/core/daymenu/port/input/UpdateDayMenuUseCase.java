/* (C) 2026 */
package aros.services.rms.core.daymenu.port.input;

import aros.services.rms.core.daymenu.domain.DayMenu;

/** Input port for updating the active day menu. */
public interface UpdateDayMenuUseCase {

  /**
   * Updates the active day menu to the given product, archiving the previous one.
   *
   * @param productId the product to set as the new day menu (must have hasOptions=true)
   * @param createdBy the authenticated user performing the update
   * @return the newly created active DayMenu
   */
  DayMenu update(Long productId, String createdBy);
}
