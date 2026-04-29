/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.SupplyCategoryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for supply categories. */
@Repository
public interface SupplyCategoryRepository extends JpaRepository<SupplyCategoryEntity, Long> {

  /**
   * Finds a supply category by name (case-insensitive).
   *
   * @param name the name
   * @return the supply category entity if found
   */
  Optional<SupplyCategoryEntity> findByNameIgnoreCase(String name);
}
