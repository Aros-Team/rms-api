/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.SupplyCategoryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyCategoryRepository extends JpaRepository<SupplyCategoryEntity, Long> {

  Optional<SupplyCategoryEntity> findByNameIgnoreCase(String name);
}
