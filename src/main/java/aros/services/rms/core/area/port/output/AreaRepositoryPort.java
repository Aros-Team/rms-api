/* (C) 2026 */
package aros.services.rms.core.area.port.output;

import aros.services.rms.core.area.domain.Area;
import java.util.List;
import java.util.Optional;

/**
 * Output port for area persistence operations.
 */
public interface AreaRepositoryPort {
  Area save(Area area);

  Optional<Area> findById(Long id);

  Optional<Area> findByName(String name);

  List<Area> findAll();

  void deleteById(Long id);

  boolean existsById(Long id);
}
