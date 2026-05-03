/* (C) 2026 */

package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.Supplier;

/** Input port: creates a new supplier. */
public interface CreateSupplierUseCase {

  /**
   * Creates a new supplier.
   *
   * @param supplier the supplier to create
   * @return the created supplier
   */
  Supplier create(Supplier supplier);
}
