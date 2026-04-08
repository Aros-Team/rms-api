/* (C) 2026 */
package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.Supplier;

/** Input port: updates an existing supplier. */
public interface UpdateSupplierUseCase {

  Supplier update(Long id, Supplier supplier);
}
