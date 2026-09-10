/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Groups related system configuration entries.
 *
 * @param id unique identifier
 * @param name group display name
 * @param description human-readable description
 * @param sortOrder display order among groups
 */
@Data
@Builder
public class SystemConfigurationGroup {
  private Long id;
  private String name;
  private String description;
  private Integer sortOrder;
}
