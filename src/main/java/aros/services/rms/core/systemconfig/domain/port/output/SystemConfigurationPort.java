/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.output;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import java.util.List;
import java.util.Optional;

/** Output port for system configuration persistence operations. */
public interface SystemConfigurationPort {

  /**
   * Finds a configuration entry by its key.
   *
   * @param key the configuration key
   * @return the configuration if found
   */
  Optional<SystemConfiguration> findByKey(String key);

  /**
   * Returns all configuration entries belonging to a group.
   *
   * @param groupId the group id
   * @return list of matching configurations
   */
  List<SystemConfiguration> findByGroupId(Long groupId);

  /**
   * Returns all configuration groups.
   *
   * @return list of all groups
   */
  List<SystemConfigurationGroup> findAllGroups();

  /**
   * Saves a single configuration entry.
   *
   * @param config the configuration to save
   * @return the saved configuration
   */
  SystemConfiguration save(SystemConfiguration config);

  /**
   * Saves multiple configuration entries.
   *
   * @param configs the configurations to save
   * @return the saved configurations
   */
  List<SystemConfiguration> saveAll(List<SystemConfiguration> configs);
}
