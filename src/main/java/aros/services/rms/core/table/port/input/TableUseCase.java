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

  /**
   * Creates a new table with AVAILABLE status.
   *
   * @param table the table data to create
   * @return the created table with generated ID
   */
  Table create(Table table);

  /**
   * Updates an existing table.
   *
   * @param id the table identifier
   * @param table the table data with updates
   * @return the updated table
   */
  Table update(Long id, Table table);

  /**
   * Retrieves all tables.
   *
   * @return list of all tables
   */
  List<Table> findAll();

  /**
   * Finds a table by its identifier.
   *
   * @param id the table identifier
   * @return the found table
   */
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
