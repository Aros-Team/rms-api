/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import java.util.List;

/** Use case for creating or updating system configuration entries. */
public interface UpdateSystemConfigurationUseCase {

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
