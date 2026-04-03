/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.daymenu.application.usecases.GetCurrentDayMenuUseCaseImpl;
import aros.services.rms.core.daymenu.application.usecases.GetDayMenuHistoryUseCaseImpl;
import aros.services.rms.core.daymenu.application.usecases.UpdateDayMenuUseCaseImpl;
import aros.services.rms.core.daymenu.port.input.GetCurrentDayMenuUseCase;
import aros.services.rms.core.daymenu.port.input.GetDayMenuHistoryUseCase;
import aros.services.rms.core.daymenu.port.input.UpdateDayMenuUseCase;
import aros.services.rms.core.daymenu.port.output.DayMenuHistoryRepositoryPort;
import aros.services.rms.core.daymenu.port.output.DayMenuRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the day menu module. */
@Configuration
public class DayMenuConfigBeans {

  @Bean
  public UpdateDayMenuUseCase updateDayMenuUseCase(
      ProductRepositoryPort productRepositoryPort,
      DayMenuRepositoryPort dayMenuRepositoryPort,
      DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort,
      Logger logger) {
    return new UpdateDayMenuUseCaseImpl(
        productRepositoryPort, dayMenuRepositoryPort, dayMenuHistoryRepositoryPort, logger);
  }

  @Bean
  public GetCurrentDayMenuUseCase getCurrentDayMenuUseCase(
      DayMenuRepositoryPort dayMenuRepositoryPort) {
    return new GetCurrentDayMenuUseCaseImpl(dayMenuRepositoryPort);
  }

  @Bean
  public GetDayMenuHistoryUseCase getDayMenuHistoryUseCase(
      DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort) {
    return new GetDayMenuHistoryUseCaseImpl(dayMenuHistoryRepositoryPort);
  }
}
