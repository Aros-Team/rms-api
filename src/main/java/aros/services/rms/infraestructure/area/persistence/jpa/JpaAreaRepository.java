/* (C) 2026 */
package aros.services.rms.infraestructure.area.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAreaRepository extends JpaRepository<AreaEntity, Long> {
  List<AreaEntity> findByIdIn(List<Long> ids);
}
