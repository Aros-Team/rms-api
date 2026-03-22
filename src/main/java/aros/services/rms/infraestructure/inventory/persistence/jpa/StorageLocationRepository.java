/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.StorageLocationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageLocationRepository extends JpaRepository<StorageLocationEntity, Long> {

  Optional<StorageLocationEntity> findByName(String name);
}
