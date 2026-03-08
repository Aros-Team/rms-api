/* (C) 2026 */
package aros.services.rms.core.table.port.input;

import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import java.util.List;

/**
 * Input port for table management operations. Handles CRUD and status lifecycle (available,
 * occupied, reserved).
 */
public interface TableUseCase {

  Table create(Table table);

  Table update(Long id, Table table);

  List<Table> findAll();

  Table findById(Long id);

  /**
   * Changes the table status to the specified value.
   *
   * @param id the table id
   * @param status the new status (AVAILABLE, OCCUPIED, RESERVED)
   * @return the updated table
   */
  Table changeStatus(Long id, TableStatus status);
}