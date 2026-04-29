/* (C) 2026 */

package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.SupplyVariant;
import java.util.Optional;

/** Output port for supply variant persistence operations. */
public interface SupplyVariantRepositoryPort {

  /**
   * Checks if supply variant exists by ID.
   *
   * @param id the supply variant ID
   * @return true if exists
   */
  boolean existsById(Long id);

  /**
   * Finds supply variant by ID.
   *
   * @param id the supply variant ID
   * @return optional supply variant
   */
  Optional<SupplyVariant> findById(Long id);
}
