/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.inventory.domain.StorageLocation;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.infraestructure.inventory.persistence.jpa.StorageLocationMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.StorageLocationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter that connects StorageLocationRepositoryPort with JPA repository. */
@Component
@RequiredArgsConstructor
public class StorageLocationPersistenceAdapter implements StorageLocationRepositoryPort {

  private final StorageLocationRepository storageLocationRepository;
  private final StorageLocationMapper storageLocationMapper;

  @Override
  public Optional<StorageLocation> findByName(String name) {
    return storageLocationRepository.findByName(name).map(storageLocationMapper::toDomain);
  }
}
