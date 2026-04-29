/* (C) 2026 */

package aros.services.rms.core.daymenu.application.service;

import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.core.daymenu.port.input.GetDayMenuHistoryUseCase;
import aros.services.rms.core.daymenu.port.output.DayMenuHistoryRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Use case implementation for retrieving paginated day menu history. */
public class GetDayMenuHistoryService implements GetDayMenuHistoryUseCase {

  private final DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort;

  /**
   * Creates a new service instance.
   *
   * @param dayMenuHistoryRepositoryPort the day menu history repository port
   */
  public GetDayMenuHistoryService(DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort) {
    this.dayMenuHistoryRepositoryPort = dayMenuHistoryRepositoryPort;
  }

  /**
   * Retrieves paginated day menu history.
   *
   * @param pageable pagination parameters
   * @return page of day menu history entries
   */
  @Override
  public Page<DayMenuHistory> getHistory(Pageable pageable) {
    return dayMenuHistoryRepositoryPort.findAll(pageable);
  }
}
