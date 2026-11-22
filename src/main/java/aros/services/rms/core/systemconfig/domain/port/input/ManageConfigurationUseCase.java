/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;

/**
 * Use case for configuration group CRUD and per-entry operations.
 *
 * <p>The legacy bulk {@code PUT /config} upsert remains in {@link UpdateSystemConfigurationUseCase}
 * and is not affected by this use case.
 */
public interface ManageConfigurationUseCase {

  /**
   * Command describing a configuration group.
   *
   * @param name group display name
   * @param description optional description
   * @param sortOrder optional display order
   */
  record GroupCommand(String name, String description, Integer sortOrder) {}

  /**
   * Command describing a partial configuration entry update.
   *
   * @param value optional new value
   * @param description optional new description
   * @param groupId optional target group
   * @param sortOrder optional display order
   */
  record EntryUpdate(String value, String description, Long groupId, Integer sortOrder) {}

  /**
   * Creates a configuration group.
   *
   * @param command the group data
   * @return the created group
   */
  SystemConfigurationGroup createGroup(GroupCommand command);

  /**
   * Renames, re-describes or reorders a configuration group.
   *
   * @param id the group id
   * @param command the new data; null fields keep their current value
   * @return the updated group
   */
  SystemConfigurationGroup updateGroup(Long id, GroupCommand command);

  /**
   * Deletes an empty configuration group.
   *
   * @param id the group id
   */
  void deleteGroup(Long id);

  /**
   * Updates a single existing configuration entry.
   *
   * @param key the configuration key
   * @param update the partial update
   * @return the saved entry
   */
  SystemConfiguration updateEntry(String key, EntryUpdate update);

  /**
   * Deletes a single configuration entry.
   *
   * @param key the configuration key
   */
  void deleteEntry(String key);
}
