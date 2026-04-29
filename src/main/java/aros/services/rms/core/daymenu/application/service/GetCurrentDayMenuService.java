/* (C) 2026 */

package aros.services.rms.core.daymenu.application.service;

import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.port.input.GetCurrentDayMenuUseCase;
import aros.services.rms.core.daymenu.port.output.DayMenuRepositoryPort;
import java.util.Optional;

/** Use case implementation for retrieving the currently active day menu. */
public class GetCurrentDayMenuService implements GetCurrentDayMenuUseCase {

  private final DayMenuRepositoryPort dayMenuRepositoryPort;

  /**
   * Creates a new service instance.
   *
   * @param dayMenuRepositoryPort the day menu repository port
   */
  public GetCurrentDayMenuService(DayMenuRepositoryPort dayMenuRepositoryPort) {
    this.dayMenuRepositoryPort = dayMenuRepositoryPort;
  }

  /**
   * Retrieves the currently active day menu.
   *
   * @return Optional containing the active day menu if exists
   */
  @Override
  public Optional<DayMenu> getCurrent() {
    return dayMenuRepositoryPort.findActive();
  }
}
