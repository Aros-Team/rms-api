/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.InventoryMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for inventory movements. */
@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovementEntity, Long> {}
