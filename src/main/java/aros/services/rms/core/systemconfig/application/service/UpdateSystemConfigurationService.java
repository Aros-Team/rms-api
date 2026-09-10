/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service implementation for creating or updating system configuration entries. */
@Service
@RequiredArgsConstructor
public class UpdateSystemConfigurationService implements UpdateSystemConfigurationUseCase {

  private final SystemConfigurationPort systemConfigurationPort;

  @Override
  @Transactional
  public SystemConfiguration save(SystemConfiguration config) {
    validateConfig(config);
    config.setUpdatedAt(Instant.now());
    return systemConfigurationPort.save(config);
  }

  @Override
  @Transactional
  public List<SystemConfiguration> saveAll(List<SystemConfiguration> configs) {
    configs.forEach(this::validateConfig);
    configs.forEach(c -> c.setUpdatedAt(Instant.now()));
    return systemConfigurationPort.saveAll(configs);
  }

  private void validateConfig(SystemConfiguration config) {
    if (config.getKey() == null || config.getKey().isBlank()) {
      throw new InvalidConfigurationException("key", "null or blank");
    }
    if (config.getValue() == null || config.getValue().isBlank()) {
      throw new InvalidConfigurationException(config.getKey(), "null or blank");
    }
  }
}
