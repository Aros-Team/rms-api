/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.infraestructure.purchase.persistence.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for SupplierEntity. */
public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, Long> {}
