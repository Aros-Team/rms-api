/* (C) 2026 */

package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.Supplier;

/** Input port: updates an existing supplier. */
public interface UpdateSupplierUseCase {

  /**
   * Updates an existing supplier.
   *
   * @param id supplier ID
   * @param supplier updated supplier data
   * @return the updated supplier
   */
  Supplier update(Long id, Supplier supplier);
}
