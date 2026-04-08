/* (C) 2026 */
package aros.services.rms.core.purchase.port.input;

import aros.services.rms.core.purchase.domain.Supplier;
import java.util.List;

/** Input port: retrieves supplier information. */
public interface GetSuppliersUseCase {

  List<Supplier> findAll();

  Supplier findById(Long id);
}
