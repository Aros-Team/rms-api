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

  /**
   * Creates the update day menu use case.
   *
   * @param productRepositoryPort the product repository port
   * @param dayMenuRepositoryPort the day menu repository port
   * @param dayMenuHistoryRepositoryPort the day menu history repository port
   * @param logger the logger
   * @return the update day menu use case
   */
  @Bean
  public UpdateDayMenuUseCase updateDayMenuUseCase(
      ProductRepositoryPort productRepositoryPort,
      DayMenuRepositoryPort dayMenuRepositoryPort,
      DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort,
      Logger logger) {
    return new UpdateDayMenuService(
        productRepositoryPort, dayMenuRepositoryPort, dayMenuHistoryRepositoryPort, logger);
  }

  /**
   * Creates the get current day menu use case.
   *
   * @param dayMenuRepositoryPort the day menu repository port
   * @return the get current day menu use case
   */
  @Bean
  public GetCurrentDayMenuUseCase getCurrentDayMenuUseCase(
      DayMenuRepositoryPort dayMenuRepositoryPort) {
    return new GetCurrentDayMenuService(dayMenuRepositoryPort);
  }

  /**
   * Creates the get day menu history use case.
   *
   * @param dayMenuHistoryRepositoryPort the day menu history repository port
   * @return the get day menu history use case
   */
  @Bean
  public GetDayMenuHistoryUseCase getDayMenuHistoryUseCase(
      DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort) {
    return new GetDayMenuHistoryService(dayMenuHistoryRepositoryPort);
  }
}
