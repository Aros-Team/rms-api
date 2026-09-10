/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.config;

import aros.services.rms.core.systemconfig.application.service.GetSystemConfigurationService;
import aros.services.rms.core.systemconfig.application.service.UpdateSystemConfigurationService;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration of beans for the system configuration module. */
@Configuration
@RequiredArgsConstructor
public class SystemConfigurationConfigBeans {

  private final GetSystemConfigurationService getSystemConfigurationService;
  private final UpdateSystemConfigurationService updateSystemConfigurationService;

  /** Creates bean for system configuration query use case. */
  @Bean
  public GetSystemConfigurationUseCase getSystemConfigurationUseCase() {
    return getSystemConfigurationService;
  }

  /** Creates bean for system configuration update use case. */
  @Bean
  public UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase() {
    return updateSystemConfigurationService;
  }
}
