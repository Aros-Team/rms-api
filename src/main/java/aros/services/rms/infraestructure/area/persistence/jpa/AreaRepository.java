/* (C) 2026 */

package aros.services.rms.infraestructure.area.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for Area entity persistence operations. */
@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {

  /** Finds an area by its name. */
  Optional<Area> findByName(String name);

  /**
   * Finds areas by IDs.
   *
   * @param ids the list of IDs
   * @return the list of areas
   */
  List<Area> findByIdIn(List<Long> ids);
}
