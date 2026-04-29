/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.StorageLocationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for storage locations. */
@Repository
public interface StorageLocationRepository extends JpaRepository<StorageLocationEntity, Long> {

  /**
   * Finds a storage location by name.
   *
   * @param name the name
   * @return the storage location entity if found
   */
  Optional<StorageLocationEntity> findByName(String name);
}
