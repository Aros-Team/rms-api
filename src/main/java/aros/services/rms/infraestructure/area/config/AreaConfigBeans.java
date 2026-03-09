/* (C) 2026 */
package aros.services.rms.infraestructure.area.config;

import aros.services.rms.core.area.application.usecases.AreaUseCaseImpl;
import aros.services.rms.core.area.port.input.AreaUseCase;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the area module. Registers area use case. */
@Configuration
public class AreaConfigBeans {

  /** Creates bean for area management use case. */
  @Bean
  public AreaUseCase areaUseCase(AreaRepositoryPort areaRepositoryPort, Logger logger) {
    return new AreaUseCaseImpl(areaRepositoryPort, logger);
  }
}
