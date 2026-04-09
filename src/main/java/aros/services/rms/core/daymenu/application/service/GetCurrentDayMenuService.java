/* (C) 2026 */
package aros.services.rms.core.daymenu.application.service;

import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.port.input.GetCurrentDayMenuUseCase;
import aros.services.rms.core.daymenu.port.output.DayMenuRepositoryPort;
import java.util.Optional;

/** Use case implementation for retrieving the currently active day menu. */
public class GetCurrentDayMenuService implements GetCurrentDayMenuUseCase {

  private final DayMenuRepositoryPort dayMenuRepositoryPort;

  public GetCurrentDayMenuService(DayMenuRepositoryPort dayMenuRepositoryPort) {
    this.dayMenuRepositoryPort = dayMenuRepositoryPort;
  }

  @Override
  public Optional<DayMenu> getCurrent() {
    return dayMenuRepositoryPort.findActive();
  }
}
