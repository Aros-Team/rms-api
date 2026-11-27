/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for the singleton floor layout. */
@Repository
public interface FloorLayoutJpaRepository extends JpaRepository<FloorLayoutEntity, Long> {}
