/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for SystemConfigurationGroupEntity persistence. */
@Repository
public interface SystemConfigurationGroupJpaRepository
    extends JpaRepository<SystemConfigurationGroupEntity, Long> {

  /**
   * Returns all configuration groups ordered by sort order.
   *
   * @return list of all groups
   */
  List<SystemConfigurationGroupEntity> findAllByOrderBySortOrder();
}
