/* (C) 2026 */

package aros.services.rms.infraestructure.area.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

  /**
   * checks if a list of areas exists by id
   *
   * @param ids the areas's id to check
   * @return true if all the areas exists
   */
  @Query("SELECT COUNT(a.id) = :size FROM Area a WHERE a.id IN :ids")
  boolean existsAllByIdIn(Set<Long> ids, long size);
}
