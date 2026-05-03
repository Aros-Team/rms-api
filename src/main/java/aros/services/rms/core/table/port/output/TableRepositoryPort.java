/* (C) 2026 */

package aros.services.rms.core.table.port.output;

import aros.services.rms.core.table.domain.Table;
import java.util.List;
import java.util.Optional;

/** Output port for table persistence operations. */
public interface TableRepositoryPort {
  /**
   * Finds a table by its identifier.
   *
   * @param id the table identifier
   * @return Optional containing the table if found
   */
  Optional<Table> findById(Long id);

  /**
   * Saves a table to the repository.
   *
   * @param table the table to save
   * @return the saved table with generated ID
   */
  Table save(Table table);

  /**
   * Retrieves all tables.
   *
   * @return list of all tables
   */
  List<Table> findAll();

  /**
   * Checks if a table with the given number exists.
   *
   * @param tableNumber the table number to check
   * @return true if a table with the given number exists
   */
  boolean existsByTableNumber(Integer tableNumber);
}
