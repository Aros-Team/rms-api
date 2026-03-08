/* (C) 2026 */
package aros.services.rms.infraestructure.area.api.dto;

import aros.services.rms.core.area.domain.Area;

/**
 * Response DTO for area data.
 *
 * @param id the area id
 * @param name the area name
 * @param type the area type
 * @param enabled whether the area is enabled
 */
public record AreaResponse(Long id, String name, String type, boolean enabled) {

  /** Converts a domain Area to an AreaResponse DTO. */
  public static AreaResponse fromDomain(Area area) {
    if (area == null) return null;
    return new AreaResponse(
        area.getId(),
        area.getName(),
        area.getType() != null ? area.getType().name() : null,
        area.isEnabled());
  }
}