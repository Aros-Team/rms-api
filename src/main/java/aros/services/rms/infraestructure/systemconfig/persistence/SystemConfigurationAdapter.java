/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

  /** Saves a single configuration entry, updating it in place when the key already exists. */
  @Override
  public SystemConfiguration save(SystemConfiguration config) {
    return mapper.toDomain(upsert(config));
  }

  /** Saves multiple configuration entries, updating existing keys in place. */
  @Override
  public List<SystemConfiguration> saveAll(List<SystemConfiguration> configs) {
    List<SystemConfiguration> saved = new ArrayList<>(configs.size());
    for (SystemConfiguration config : configs) {
      saved.add(mapper.toDomain(upsert(config)));
    }
    return saved;
  }

  /**
   * Inserts or updates one entry by key.
   *
   * <p>Keys are unique in the table, so a blind insert would either fail or create a duplicate.
   * When the key already exists its metadata (description, group, sort order, audit actor) is
   * preserved unless the request explicitly overrides it. New keys are appended to the first group
   * so they remain visible in the grouped GET response.
   */
  private SystemConfigurationEntity upsert(SystemConfiguration config) {
    SystemConfigurationEntity entity =
        configurationRepo.findByKey(config.getKey()).orElseGet(SystemConfigurationEntity::new);

    entity.setKey(config.getKey());
    entity.setValue(config.getValue());

    if (config.getDescription() != null) {
      entity.setDescription(config.getDescription());
    }

    Long groupId = config.getGroupId();
    if (groupId == null && entity.getGroupId() == null) {
      groupId = defaultGroupId();
    }
    if (groupId != null) {
      entity.setGroupId(groupId);
    }

    if (config.getSortOrder() != null) {
      entity.setSortOrder(config.getSortOrder());
    } else if (entity.getSortOrder() == null) {
      entity.setSortOrder(nextSortOrder(entity.getGroupId()));
    }

    if (config.getUpdatedBy() != null) {
      entity.setUpdatedBy(config.getUpdatedBy());
    }
    entity.setUpdatedAt(config.getUpdatedAt() != null ? config.getUpdatedAt() : Instant.now());

    return configurationRepo.save(entity);
  }

  private Long defaultGroupId() {
    return groupRepo.findAllByOrderBySortOrder().stream()
        .findFirst()
        .map(SystemConfigurationGroupEntity::getId)
        .orElse(null);
  }

  private int nextSortOrder(Long groupId) {
    if (groupId == null) {
      return 0;
    }
    return configurationRepo.findByGroupId(groupId).stream()
            .map(SystemConfigurationEntity::getSortOrder)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .max()
            .orElse(-1)
        + 1;
  }
}
