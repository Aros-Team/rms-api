/* (C) 2026 */

package aros.services.rms.infraestructure.table.persistence.jpa;

import aros.services.rms.infraestructure.table.persistence.Table;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for Table entity persistence operations. */
@Repository
public interface TableRepository extends JpaRepository<Table, Long> {

  /** Checks if a table with the given number exists. */
  boolean existsByTableNumber(Integer tableNumber);

  /**
   * Finds tables whose table number contains the given string (case-insensitive). Uses CAST to
   * convert the integer tableNumber to string for LIKE matching.
   *
   * @param tableNumber the table number substring
   * @return the list of matching tables
   */
  @Query(
      "SELECT t FROM Table t WHERE CAST(t.tableNumber AS string) LIKE"
          + " CONCAT('%', :tableNumber, '%')")
  List<Table> findByTableNumberContainingIgnoreCase(@Param("tableNumber") String tableNumber);
}
