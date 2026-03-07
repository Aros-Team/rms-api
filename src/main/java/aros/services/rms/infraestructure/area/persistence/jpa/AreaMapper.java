/* (C) 2026 */
package aros.services.rms.infraestructure.area.persistence.jpa;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaType;
import org.springframework.stereotype.Component;

@Component
public class AreaMapper {

  public aros.services.rms.infraestructure.area.persistence.jpa.Area toEntity(Area domain) {
    if (domain == null) return null;

    return aros.services.rms.infraestructure.area.persistence.jpa.Area.builder()
        .id(domain.getId())
        .name(domain.getName())
        .type(
            domain.getType() != null
                ? aros.services.rms.infraestructure.area.persistence.AreaType.valueOf(
                    domain.getType().name())
                : null)
        .build();
  }

  public Area toDomain(aros.services.rms.infraestructure.area.persistence.jpa.Area entity) {
    if (entity == null) return null;

    return Area.builder()
        .id(entity.getId())
        .name(entity.getName())
        .type(entity.getType() != null ? AreaType.valueOf(entity.getType().name()) : null)
        .build();
  }
}
