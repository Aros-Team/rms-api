/* (C) 2026 */
package aros.services.rms.infraestructure.area.persistence.jpa;

import java.util.List;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AreaRepositoryAdapter implements AreaRepositoryPort {

  private final JpaAreaRepository internal;
  private final AreaPersistenceMapper mapper;

  @Override
  public List<Area> findByIdIn(List<AreaId> ids) {
    List<Long> longIds = ids.stream()
        .map(AreaId::value)
        .toList();
    return internal.findByIdIn(longIds).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
