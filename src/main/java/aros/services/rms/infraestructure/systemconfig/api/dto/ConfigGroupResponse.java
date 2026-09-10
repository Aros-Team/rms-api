/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO for a configuration group with its entries. */
@Schema(description = "Response DTO for a configuration group with its entries")
public record ConfigGroupResponse(
    @Schema(description = "Group ID", example = "1") Long id,
    @Schema(description = "Group name", example = "General") String name,
    @Schema(description = "Group description", example = "Moneda, zona horaria, pais")
        String description,
    @Schema(description = "Sort order", example = "1") Integer sortOrder,
    @Schema(description = "Configuration entries in this group")
        List<ConfigEntryResponse> entries) {

  /** Creates a ConfigGroupResponse from a SystemConfigurationGroup domain object. */
  public static ConfigGroupResponse fromDomain(
      SystemConfigurationGroup group, List<ConfigEntryResponse> entries) {
    if (group == null) {
      return null;
    }
    return new ConfigGroupResponse(
        group.getId(),
        group.getName(),
        group.getDescription(),
        group.getSortOrder(),
        entries != null ? entries : List.of());
  }
}
