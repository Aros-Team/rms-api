/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for SystemConfigurationPort. */
@Repository
@RequiredArgsConstructor
@Transactional
public class SystemConfigurationAdapter implements SystemConfigurationPort {

  private final SystemConfigurationJpaRepository configurationRepo;
  private final SystemConfigurationGroupJpaRepository groupRepo;
  private final SystemConfigurationMapper mapper;

  /** Finds a configuration entry by its key. */
  @Override
  public Optional<SystemConfiguration> findByKey(String key) {
    return configurationRepo.findByKey(key).map(mapper::toDomain);
  }

  /** Returns all configuration entries belonging to a group. */
  @Override
  public List<SystemConfiguration> findByGroupId(Long groupId) {
    return configurationRepo.findByGroupId(groupId).stream().map(mapper::toDomain).toList();
  }

  /** Returns all configuration groups. */
  @Override
  public List<SystemConfigurationGroup> findAllGroups() {
    return groupRepo.findAllByOrderBySortOrder().stream().map(mapper::toDomainGroup).toList();
  }

  /** Saves a single configuration entry. */
  @Override
  public SystemConfiguration save(SystemConfiguration config) {
    SystemConfigurationEntity entity = mapper.toEntity(config);
    entity = configurationRepo.save(entity);
    return mapper.toDomain(entity);
  }

  /** Saves multiple configuration entries. */
  @Override
  public List<SystemConfiguration> saveAll(List<SystemConfiguration> configs) {
    List<SystemConfigurationEntity> entities = configs.stream().map(mapper::toEntity).toList();
    return configurationRepo.saveAll(entities).stream().map(mapper::toDomain).toList();
  }
}
