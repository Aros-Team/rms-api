/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.config;

import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.floorlayout.application.service.FloorLayoutService;
import aros.services.rms.core.floorlayout.port.input.FloorLayoutUseCase;
import aros.services.rms.core.floorlayout.port.output.FloorLayoutRepositoryPort;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the floor layout module. */
@Configuration
public class FloorLayoutConfigBeans {

  /**
   * Creates the floor layout use case.
   *
   * @param floorLayoutRepositoryPort layout persistence
   * @param areaRepositoryPort existing area lookup
   * @param tableRepositoryPort existing table lookup
   * @return floor layout use case
   */
  @Bean
  public FloorLayoutUseCase floorLayoutUseCase(
      FloorLayoutRepositoryPort floorLayoutRepositoryPort,
      AreaRepositoryPort areaRepositoryPort,
      TableRepositoryPort tableRepositoryPort) {
    return new FloorLayoutService(
        floorLayoutRepositoryPort, areaRepositoryPort, tableRepositoryPort);
  }
}
