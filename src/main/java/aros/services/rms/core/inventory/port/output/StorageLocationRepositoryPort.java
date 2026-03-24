/* (C) 2026 */
package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.StorageLocation;
import java.util.Optional;

/** Output port for storage location persistence operations. */
public interface StorageLocationRepositoryPort {

  Optional<StorageLocation> findByName(String name);
}
