/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import java.util.List;
import java.util.Optional;

/** Use case for querying system configuration entries and groups. */
public interface GetSystemConfigurationUseCase {

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
}
