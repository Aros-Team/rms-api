/* (C) 2026 */
package aros.services.rms.config;

import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.infraestructure.area.persistence.jpa.AreaPersistenceMapper;
import aros.services.rms.infraestructure.area.persistence.jpa.AreaRepositoryAdapter;
import aros.services.rms.infraestructure.area.persistence.jpa.JpaAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AreaBeanConfig {

  private final JpaAreaRepository internal;
  private final AreaPersistenceMapper mapper;

  @Bean
  public AreaRepositoryPort areaRepositoryPort() {
    return new AreaRepositoryAdapter(internal, mapper);
  }
}
