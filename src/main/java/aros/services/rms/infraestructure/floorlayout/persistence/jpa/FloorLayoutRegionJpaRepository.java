/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for floor layout regions. */
@Repository
public interface FloorLayoutRegionJpaRepository extends JpaRepository<FloorRegionEntity, Long> {

  /**
   * Finds every region of a layout in a stable order.
   *
   * @param floorLayoutId layout id
   * @return regions ordered by region key
   */
  List<FloorRegionEntity> findByFloorLayoutIdOrderByRegionKeyAsc(Long floorLayoutId);

  /**
   * Removes every region of a layout before a full replacement.
   *
   * @param floorLayoutId layout id
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("DELETE FROM FloorRegionEntity region WHERE region.floorLayoutId = :floorLayoutId")
  void deleteByFloorLayoutId(@Param("floorLayoutId") Long floorLayoutId);
}
