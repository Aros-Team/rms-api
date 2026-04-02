/* (C) 2026 */
package aros.services.rms.core.purchase.port.output;

import aros.services.rms.core.purchase.domain.Supplier;
import java.util.List;
import java.util.Optional;

/** Output port: persistence operations for Supplier. */
public interface SupplierRepositoryPort {

  Supplier save(Supplier supplier);

  Optional<Supplier> findById(Long id);

  List<Supplier> findAll();
}
