/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.exception.ConfigEntryNotFoundException;
import aros.services.rms.core.systemconfig.domain.exception.ConfigGroupAlreadyExistsException;
import aros.services.rms.core.systemconfig.domain.exception.ConfigGroupNotEmptyException;
import aros.services.rms.core.systemconfig.domain.exception.ConfigGroupNotFoundException;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.ManageConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Configuration group CRUD and per-entry operations layered over the existing adapter. */
@RequiredArgsConstructor
public class ManageConfigurationService implements ManageConfigurationUseCase {

  private final SystemConfigurationPort systemConfigurationPort;
  private final UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public SystemConfigurationGroup createGroup(GroupCommand command) {
    String name = validateName(command.name());
    if (systemConfigurationPort.findGroupByName(name).isPresent()) {
      throw new ConfigGroupAlreadyExistsException(name);
    }
    int sortOrder =
        command.sortOrder() != null
            ? command.sortOrder()
            : systemConfigurationPort.findAllGroups().stream()
                    .map(SystemConfigurationGroup::getSortOrder)
                    .filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0)
                + 1;
    return systemConfigurationPort.saveGroup(
        SystemConfigurationGroup.builder()
            .name(name)
            .description(command.description())
            .sortOrder(sortOrder)
            .build());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public SystemConfigurationGroup updateGroup(Long id, GroupCommand command) {
    SystemConfigurationGroup existing =
        systemConfigurationPort
            .findGroupById(id)
            .orElseThrow(() -> new ConfigGroupNotFoundException(String.valueOf(id)));
    if (command.name() != null) {
      String name = validateName(command.name());
      systemConfigurationPort
          .findGroupByName(name)
          .filter(group -> !group.getId().equals(id))
          .ifPresent(
              group -> {
                throw new ConfigGroupAlreadyExistsException(name);
              });
      existing.setName(name);
    }
    if (command.description() != null) {
      existing.setDescription(command.description());
    }
    if (command.sortOrder() != null) {
      existing.setSortOrder(command.sortOrder());
    }
    return systemConfigurationPort.saveGroup(existing);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void deleteGroup(Long id) {
    systemConfigurationPort
        .findGroupById(id)
        .orElseThrow(() -> new ConfigGroupNotFoundException(String.valueOf(id)));
    int entries = systemConfigurationPort.countEntriesInGroup(id);
    if (entries > 0) {
      throw new ConfigGroupNotEmptyException(id, entries);
    }
    systemConfigurationPort.deleteGroup(id);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public SystemConfiguration updateEntry(String key, EntryUpdate update) {
    SystemConfiguration existing =
        systemConfigurationPort
            .findByKey(key)
            .orElseThrow(() -> new ConfigEntryNotFoundException(key));
    if (update == null) {
      throw new InvalidConfigurationException("La actualización de la entrada es obligatoria");
    }
    if (update.value() != null) {
      existing.setValue(update.value());
    }
    if (update.description() != null) {
      existing.setDescription(update.description());
    }
    if (update.groupId() != null) {
      existing.setGroupId(update.groupId());
    }
    if (update.sortOrder() != null) {
      existing.setSortOrder(update.sortOrder());
    }
    return updateSystemConfigurationUseCase.save(existing);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void deleteEntry(String key) {
    systemConfigurationPort.findByKey(key).orElseThrow(() -> new ConfigEntryNotFoundException(key));
    systemConfigurationPort.deleteByKey(key);
  }

  private String validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new InvalidConfigurationException("nombre del grupo", "nulo o vacío");
    }
    return name.trim();
  }
}
