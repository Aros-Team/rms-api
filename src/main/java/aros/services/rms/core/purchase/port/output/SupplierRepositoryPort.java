/* (C) 2026 */
package aros.services.rms.core.purchase.port.output;

import aros.services.rms.core.purchase.domain.Supplier;
import java.util.List;
import java.util.Optional;

/** Output port: persistence operations for Supplier. */
public interface SupplierRepositoryPort {

  /**
   * Saves a supplier.
   *
   * @param supplier the supplier to save
   * @return the saved supplier
   */
  Supplier save(Supplier supplier);

  /**
   * Finds a supplier by ID.
   *
   * @param id the supplier ID
   * @return optional supplier
   */
  Optional<Supplier> findById(Long id);

  /**
   * Returns all suppliers.
   *
   * @return list of all suppliers
   */
  List<Supplier> findAll();
}
