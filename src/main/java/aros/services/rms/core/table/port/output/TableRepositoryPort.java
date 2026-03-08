/* (C) 2026 */
package aros.services.rms.core.table.port.output;

import aros.services.rms.core.table.domain.Table;
import java.util.List;
import java.util.Optional;

/**
 * Output port for table persistence operations.
 */
public interface TableRepositoryPort {
  Optional<Table> findById(Long id);

  Table save(Table table);

  List<Table> findAll();

  boolean existsByTableNumber(Integer tableNumber);
}
