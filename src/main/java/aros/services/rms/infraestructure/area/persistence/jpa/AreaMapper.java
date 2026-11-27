/* (C) 2026 */

package aros.services.rms.infraestructure.area.persistence.jpa;

import aros.services.rms.core.area.domain.Area;
import org.springframework.stereotype.Component;

/** Mapper between Area domain model and Area JPA entity. */
@Component
public class AreaMapper {

  /** Converts a domain Area to a JPA entity. */
  public aros.services.rms.infraestructure.area.persistence.jpa.Area toEntity(Area domain) {
    if (domain == null) {
      return null;
    }

    return aros.services.rms.infraestructure.area.persistence.jpa.Area.builder()
        .id(domain.getId())
        .name(domain.getName())
        .activities(
            domain.getActivities() == null
                ? new java.util.HashSet<>()
                : new java.util.HashSet<>(domain.getActivities()))
        .enabled(domain.isEnabled())
        .exclusive(domain.isExclusive())
        .build();
  }

  /** Converts a JPA entity Area to a domain model. */
  public Area toDomain(aros.services.rms.infraestructure.area.persistence.jpa.Area entity) {
    if (entity == null) {
      return null;
    }

    return Area.builder()
        .id(entity.getId())
        .name(entity.getName())
        .activities(
            entity.getActivities() == null
                ? new java.util.HashSet<>()
                : new java.util.HashSet<>(entity.getActivities()))
        .enabled(entity.isEnabled())
        .exclusive(entity.isExclusive())
        .build();
  }
}
