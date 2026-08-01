/* (C) 2026 */

package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.Supplier;
import java.util.List;

/** Input port: retrieves supplier information. */
public interface GetSuppliersUseCase {

  /**
   * Returns all suppliers.
   *
   * @return list of all suppliers
   */
  List<Supplier> findAll();

  /**
   * Finds suppliers whose names contain the supplied text, ignoring case.
   *
   * @param name partial supplier name
   * @return matching suppliers
   */
  List<Supplier> findByNameContainingIgnoreCase(String name);

  /**
   * Finds a supplier by ID.
   *
   * @param id supplier ID
   * @return the supplier
   */
  Supplier findById(Long id);
}
