/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Response DTO for a configuration entry. */
@Schema(description = "Response DTO for a configuration entry")
public record ConfigEntryResponse(
    @Schema(description = "Configuration ID", example = "1") Long id,
    @Schema(description = "Configuration key", example = "default_currency") String key,
    @Schema(description = "Configuration value", example = "COP") String value,
    @Schema(description = "Configuration description", example = "Moneda del sistema")
        String description,
    @Schema(description = "Group ID this entry belongs to", example = "1") Long groupId,
    @Schema(description = "Sort order within group", example = "1") Integer sortOrder,
    @Schema(description = "Last update timestamp", example = "2026-08-03T10:30:00Z")
        Instant updatedAt) {

  /** Creates a ConfigEntryResponse from a SystemConfiguration domain object. */
  public static ConfigEntryResponse fromDomain(SystemConfiguration config) {
    if (config == null) {
      return null;
    }
    return new ConfigEntryResponse(
        config.getId(),
        config.getKey(),
        config.getValue(),
        config.getDescription(),
        config.getGroupId(),
        config.getSortOrder(),
        config.getUpdatedAt());
  }
}
