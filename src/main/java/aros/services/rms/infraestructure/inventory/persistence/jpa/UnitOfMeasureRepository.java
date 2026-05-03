/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.UnitOfMeasureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for unit of measures. */
@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasureEntity, Long> {}
