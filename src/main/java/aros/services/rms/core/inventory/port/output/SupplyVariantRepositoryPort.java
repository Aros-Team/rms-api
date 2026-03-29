/* (C) 2026 */
package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.SupplyVariant;
import java.util.Optional;

/** Output port for supply variant persistence operations. */
public interface SupplyVariantRepositoryPort {

  boolean existsById(Long id);

  Optional<SupplyVariant> findById(Long id);
}
