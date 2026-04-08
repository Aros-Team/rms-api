/* (C) 2026 */
package aros.services.rms.core.daymenu.application.usecases;

import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.core.daymenu.port.input.GetDayMenuHistoryUseCase;
import aros.services.rms.core.daymenu.port.output.DayMenuHistoryRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Use case implementation for retrieving paginated day menu history. */
public class GetDayMenuHistoryUseCaseImpl implements GetDayMenuHistoryUseCase {

  private final DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort;

  public GetDayMenuHistoryUseCaseImpl(DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort) {
    this.dayMenuHistoryRepositoryPort = dayMenuHistoryRepositoryPort;
  }

  @Override
  public Page<DayMenuHistory> getHistory(Pageable pageable) {
    return dayMenuHistoryRepositoryPort.findAll(pageable);
  }
}
