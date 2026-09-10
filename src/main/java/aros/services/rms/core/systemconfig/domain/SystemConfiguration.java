/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * System configuration entry.
 *
 * @param id unique identifier
 * @param key configuration key
 * @param value configuration value
 * @param description human-readable description
 * @param groupId foreign key to the configuration group
 * @param sortOrder display order within the group
 * @param updatedBy user id who last updated this entry
 * @param updatedAt last update timestamp
 */
@Data
@Builder
public class SystemConfiguration {
  private Long id;
  private String key;
  private String value;
  private String description;
  private Long groupId;
  private Integer sortOrder;
  private Long updatedBy;
  private Instant updatedAt;
}
