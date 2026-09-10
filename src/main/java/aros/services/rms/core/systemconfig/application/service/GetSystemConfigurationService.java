/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service implementation for querying system configuration entries and groups. */
@Service
@RequiredArgsConstructor
public class GetSystemConfigurationService implements GetSystemConfigurationUseCase {

  private final SystemConfigurationPort systemConfigurationPort;

  @Override
  public Optional<SystemConfiguration> findByKey(String key) {
    return systemConfigurationPort.findByKey(key);
  }

  @Override
  public List<SystemConfiguration> findByGroupId(Long groupId) {
    return systemConfigurationPort.findByGroupId(groupId);
  }

  @Override
  public List<SystemConfigurationGroup> findAllGroups() {
    return systemConfigurationPort.findAllGroups();
  }
}
