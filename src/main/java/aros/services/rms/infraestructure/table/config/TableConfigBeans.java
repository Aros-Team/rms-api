/* (C) 2026 */
package aros.services.rms.infraestructure.table.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.table.application.usecases.TableUseCaseImpl;
import aros.services.rms.core.table.port.input.TableUseCase;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the table module. Registers table use case. */
@Configuration
public class TableConfigBeans {

  /** Creates bean for table management use case. */
  @Bean
  public TableUseCase tableUseCase(TableRepositoryPort tableRepositoryPort, Logger logger) {
    return new TableUseCaseImpl(tableRepositoryPort, logger);
  }
}
