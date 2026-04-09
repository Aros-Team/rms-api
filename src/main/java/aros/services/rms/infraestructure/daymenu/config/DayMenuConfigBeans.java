/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.daymenu.application.service.GetCurrentDayMenuService;
import aros.services.rms.core.daymenu.application.service.GetDayMenuHistoryService;
import aros.services.rms.core.daymenu.application.service.UpdateDayMenuService;
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
    return new UpdateDayMenuService(
        productRepositoryPort, dayMenuRepositoryPort, dayMenuHistoryRepositoryPort, logger);
  }

  @Bean
  public GetCurrentDayMenuUseCase getCurrentDayMenuUseCase(
      DayMenuRepositoryPort dayMenuRepositoryPort) {
    return new GetCurrentDayMenuService(dayMenuRepositoryPort);
  }

  @Bean
  public GetDayMenuHistoryUseCase getDayMenuHistoryUseCase(
      DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort) {
    return new GetDayMenuHistoryService(dayMenuHistoryRepositoryPort);
  }
}
