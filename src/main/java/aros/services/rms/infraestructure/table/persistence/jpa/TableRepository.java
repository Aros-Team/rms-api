/* (C) 2026 */
package aros.services.rms.infraestructure.table.persistence.jpa;

import aros.services.rms.infraestructure.table.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for Table entity persistence operations. */
@Repository
public interface TableRepository extends JpaRepository<Table, Long> {

  /** Checks if a table with the given number exists. */
  boolean existsByTableNumber(Integer tableNumber);
}
