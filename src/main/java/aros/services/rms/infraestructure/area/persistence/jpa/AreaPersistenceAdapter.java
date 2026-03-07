/* (C) 2026 */
package aros.services.rms.infraestructure.area.persistence.jpa;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AreaPersistenceAdapter implements AreaRepositoryPort {

  private final AreaRepository areaRepository;
  private final AreaMapper areaMapper;

  @Override
  public Area save(Area area) {
    aros.services.rms.infraestructure.area.persistence.jpa.Area entity = areaMapper.toEntity(area);
    aros.services.rms.infraestructure.area.persistence.jpa.Area savedEntity =
        areaRepository.save(entity);
    return areaMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Area> findById(Long id) {
    return areaRepository.findById(id).map(areaMapper::toDomain);
  }

  @Override
  public List<Area> findAll() {
    return areaRepository.findAll().stream().map(areaMapper::toDomain).collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    areaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(Long id) {
    return areaRepository.existsById(id);
  }
}
