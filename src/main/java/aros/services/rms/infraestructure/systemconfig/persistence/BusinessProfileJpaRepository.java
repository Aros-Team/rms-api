/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for the business profile singleton. */
@Repository
public interface BusinessProfileJpaRepository extends JpaRepository<BusinessProfileEntity, Long> {}
