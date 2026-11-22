/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for the seven weekday opening windows. */
public interface OpeningHoursJpaRepository extends JpaRepository<OpeningHoursEntity, String> {}
