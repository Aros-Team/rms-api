/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for SystemConfigurationEntity persistence. */
@Repository
public interface SystemConfigurationJpaRepository
    extends JpaRepository<SystemConfigurationEntity, Long> {

  /**
   * Finds a configuration entry by its key.
   *
   * @param key the configuration key
   * @return the configuration if found
   */
  Optional<SystemConfigurationEntity> findByKey(String key);

  /**
   * Returns all configuration entries belonging to a group, ordered by sort order.
   *
   * @param groupId the group id
   * @return list of matching configurations
   */
  @Query(
      "SELECT e FROM SystemConfigurationEntity e WHERE e.groupId = :groupId ORDER BY e.sortOrder")
  List<SystemConfigurationEntity> findByGroupId(@Param("groupId") Long groupId);

  /**
   * Returns all configuration entries ordered by group and sort order.
   *
   * @return list of all configurations
   */
  @Query("SELECT e FROM SystemConfigurationEntity e ORDER BY e.groupId, e.sortOrder")
  List<SystemConfigurationEntity> findAllOrdered();
}
