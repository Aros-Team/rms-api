/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for table placements. */
@Repository
public interface FloorTablePlacementJpaRepository
    extends JpaRepository<FloorTablePlacementEntity, Long> {

  /**
   * Finds every placement of a layout.
   *
   * @param floorLayoutId layout id
   * @return placements ordered by table id
   */
  List<FloorTablePlacementEntity> findByFloorLayoutIdOrderByTableIdAsc(Long floorLayoutId);

  /**
   * Removes every placement of a layout before a full replacement.
   *
   * @param floorLayoutId layout id
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("DELETE FROM FloorTablePlacementEntity p WHERE p.floorLayoutId = :floorLayoutId")
  void deleteByFloorLayoutId(@Param("floorLayoutId") Long floorLayoutId);
}
