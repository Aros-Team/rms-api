/* (C) 2026 */

package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.SupplyVariant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

  /**
   * Finds supply variants by their identifiers.
   *
   * @param ids the supply variant identifiers
   * @return list of supply variants matching the given IDs
   */
  List<SupplyVariant> findAllById(List<Long> ids);

  /**
   * Finds supply variants whose supply name contains the given string (case-insensitive),
   * paginated.
   *
   * @param name the name substring to search for
   * @param pageable pagination information
   * @return page of matching supply variants
   */
  Page<SupplyVariant> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
